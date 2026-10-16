package com.example.ragdemo.rag;

import com.example.ragdemo.chat.ChatHistoryService;
import com.example.ragdemo.rag.FlowRecorder.Actor;
import com.example.ragdemo.system1.LayaClient;
import com.example.ragdemo.system1.SystemOneFilter;
import com.example.ragdemo.system1.SystemOneReranker;
import com.example.ragdemo.system2.SystemTwoClient;
import com.example.ragdemo.weaviate.HybridRetriever;
import com.example.ragdemo.weaviate.LearnedAnswers;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static com.example.ragdemo.rag.RagServiceTest.HITS;
import static com.example.ragdemo.rag.RagServiceTest.SCORES;
import static com.example.ragdemo.rag.RagServiceTest.laya;
import static com.example.ragdemo.rag.RagServiceTest.verifier;
import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;
import static org.assertj.core.api.Assertions.assertThat;

/** 🛠 Agentic mode with a scripted model: tool calls run by the app, numbering, limits, H2, fail-closed. */
class RagAgentTest {

    /** What the model was sent each round: the conversation, and how many tools were on offer. */
    final List<List<Message>> conversations = new ArrayList<>();
    final List<Integer> toolsOffered = new ArrayList<>();
    final List<String> searches = new ArrayList<>();

    static AssistantMessage calls(String name, String json) {
        return AssistantMessage.builder().content("")
                .toolCalls(List.of(new ToolCall("call-" + name, "function", name, json))).build();
    }

    static AssistantMessage text(String answer) {
        return AssistantMessage.builder().content(answer).build();
    }

    SystemTwoClient model(AssistantMessage... replies) {
        Deque<AssistantMessage> script = new ArrayDeque<>(List.of(replies));
        return new SystemTwoClient(null) {
            @Override
            public Step step(List<Message> conversation, List<ToolCallback> tools, String model) {
                conversations.add(List.copyOf(conversation));
                toolsOffered.add(tools.size());
                AssistantMessage next = script.size() > 1 ? script.pop() : script.peek();   // the last reply repeats
                if (tools.isEmpty() && next.hasToolCalls()) next = text("From what the tools returned: [1].");
                return new Step(next, "agent/model:free", 100, 10, 50, List.of());
            }
        };
    }

    HybridRetriever retriever(RuntimeException failure) {
        return new HybridRetriever(null) {
            @Override
            public Result search(String text, String lang, Mode mode, int limit) {
                if (failure != null) throw failure;
                searches.add(text + " [" + lang + "]");
                return new Result(HITS, 3, 40, 20, 120, lang);
            }
        };
    }

    static ChatHistoryService history(List<String> questions) {
        return new ChatHistoryService(null, null) {
            @Override
            public List<String> recentQuestions(int limit) {
                return questions.stream().limit(limit).toList();
            }
        };
    }

    static LearnedAnswers noLearned() {
        return new LearnedAnswers(null) {
            @Override
            public java.util.Optional<Learned> closest(String question) {
                return java.util.Optional.empty();
            }

            @Override
            public String save(String q, String a, String m, List<LearnedSource> s, double x, double y, double z) {
                return "id";
            }
        };
    }

    RagService rag(SystemTwoClient model, HybridRetriever retriever, Map<String, LayaClient.Scores> scores,
                   RuntimeException layaDown, List<String> history) {
        LearnedAnswers noLearned = noLearned();
        RagAgent agent = new RagAgent(model, retriever, laya(scores, layaDown), noLearned, verifier(true, 0.9),
                history(history));
        return new RagService(retriever, laya(scores, layaDown), new SystemOneReranker(), model, noLearned,
                verifier(true, 0.9), agent);
    }

    @Test
    void theModelSearches_theAppRunsTheToolWithSystemOne_andTheAnswerCitesTheKeptPassage() {
        RagAnswer a = rag(model(calls("search_passages", "{\"query\":\"reset AER-01\"}"), text("Hold RESET for 5 s [1].")),
                retriever(null), SCORES, null, List.of()).ask("How do I reset AER-01?", true, p -> { });

        assertThat(a.text()).isEqualTo("Hold RESET for 5 s [1].");
        assertThat(a.agent().rounds()).isEqualTo(2);
        assertThat(a.agent().calls()).containsExactly("search_passages: reset AER-01");
        assertThat(searches).containsExactly("reset AER-01 [en]");   // in the question's language
        // the tool result the model got: only the passage System 1 kept, numbered [1] — never the injection
        ToolResponseMessage result = (ToolResponseMessage) conversations.get(1).getLast();
        assertThat(result.getResponses().getFirst().responseData())
                .startsWith("[1] Aerator manual — Reset after a trip")
                .doesNotContain("Ignore all previous instructions", "chicken rice");
        assertThat(a.sources()).filteredOn(RagAnswer.Source::used).extracting(RagAnswer.Source::title)
                .containsExactly("Aerator manual — Reset after a trip");
        assertThat(a.flow()).extracting(FlowRecorder.FlowHop::to).contains(Actor.SYSTEM2, Actor.WEAVIATE, Actor.SYSTEM1);
        assertThat(a.trace()).extracting(RagAnswer.TraceLine::step).contains("trace.step.AGENT", "trace.step.LEARN");
        assertThat(a.learning().saved()).isTrue();   // verified and learned like any other answer
    }

    @Test
    void afterTheToolLimit_theLastRoundOffersNoTools_soTheModelMustAnswer() {
        RagAnswer a = rag(model(calls("search_passages", "{\"query\":\"again\"}")), retriever(null), SCORES, null,
                List.of()).ask("How do I reset AER-01?", true, p -> { });

        assertThat(a.agent().toolCalls()).isLessThanOrEqualTo(RagAgent.MAX_TOOL_CALLS);
        assertThat(a.agent().rounds()).isEqualTo(RagAgent.MAX_ROUNDS);
        assertThat(toolsOffered.getLast()).isZero();
        assertThat(conversations.getLast().getLast().getText()).isEqualTo(RagAgent.ANSWER_NOW);
        assertThat(a.failed()).isFalse();
    }

    @Test
    void recentQuestionsReadsHistoryFromH2_whichAppearsAsItsOwnLifeline() {
        RagAnswer a = rag(model(calls("recent_questions", "{\"limit\":2}"), text("You asked about PCR and RAS.")),
                retriever(null), SCORES, null, List.of("What is PCR?", "What does a RAS do?", "old"))
                .ask("What did I ask before?", true, p -> { });

        ToolResponseMessage result = (ToolResponseMessage) conversations.get(1).getLast();
        assertThat(result.getResponses().getFirst().responseData()).isEqualTo("- What is PCR?\n- What does a RAS do?");
        assertThat(a.flow()).extracting(FlowRecorder.FlowHop::to).contains(Actor.H2);
        assertThat(a.learning()).isNull();   // no passage was given, so there is nothing to verify it against
    }

    @Test
    void aRequiredServiceThatFailsInsideATool_stopsTheAgent_failClosed() {
        RagAnswer a = rag(model(calls("search_passages", "{\"query\":\"reset\"}"), text("never")), retriever(null),
                SCORES, new SystemDownException(SYSTEM1, "status.system1.down"), List.of())
                .ask("How do I reset AER-01?", true, p -> { });

        assertThat(a.failed()).isTrue();
        assertThat(a.failure().service()).isEqualTo(SYSTEM1);
        assertThat(conversations).hasSize(1);   // System 2 was not asked again
    }

    @Test
    void systemOneBusyAfterTheFirstSearch_theAgentAnswersFromThePassagesItHas() {
        java.util.concurrent.atomic.AtomicInteger checks = new java.util.concurrent.atomic.AtomicInteger();
        SystemOneFilter busyLater = new SystemOneFilter(new LayaClient(null) {
            @Override
            public Scores score(String question, String passage) {
                if (checks.incrementAndGet() > HITS.size()) throw new SystemDownException(SYSTEM1, "status.system1.busy");
                return SCORES.get(passage);
            }
        });
        SystemTwoClient model = model(calls("search_passages", "{\"query\":\"reset\"}"),
                calls("search_passages", "{\"query\":\"reset again\"}"), text("Hold RESET for 5 s [1]."));
        RagAgent agent = new RagAgent(model, retriever(null), busyLater, noLearned(), verifier(true, 0.9),
                history(List.of()));
        RagAnswer a = new RagService(retriever(null), busyLater, new SystemOneReranker(), model, noLearned(),
                verifier(true, 0.9), agent).ask("How do I reset AER-01?", true, p -> { });

        assertThat(a.failed()).isFalse();
        assertThat(a.text()).isEqualTo("Hold RESET for 5 s [1].");
        ToolResponseMessage second = (ToolResponseMessage) conversations.get(2).getLast();
        assertThat(second.getResponses().getFirst().responseData()).startsWith("This look-up could not be done");
    }

    @Test
    void anUnknownToolOrBadArguments_goBackToTheModel_insteadOfFailing() {
        RagAnswer a = rag(model(calls("drop_table", "{}"), text("I don't know.")), retriever(null), SCORES, null,
                List.of()).ask("How do I reset AER-01?", true, p -> { });

        ToolResponseMessage result = (ToolResponseMessage) conversations.get(1).getLast();
        assertThat(result.getResponses().getFirst().responseData()).isEqualTo("Unknown tool: drop_table");
        assertThat(a.text()).isEqualTo("I don't know.");
    }
}
