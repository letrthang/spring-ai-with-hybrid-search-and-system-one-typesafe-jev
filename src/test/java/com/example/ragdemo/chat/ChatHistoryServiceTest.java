package com.example.ragdemo.chat;

import com.example.ragdemo.rag.FlowRecorder;
import com.example.ragdemo.rag.Msg;
import com.example.ragdemo.rag.RagAnswer;
import com.example.ragdemo.rag.ServiceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:history;DB_CLOSE_DELAY=-1")
class ChatHistoryServiceTest {

    @Autowired
    ChatHistoryService history;

    static RagAnswer answer(String question, String text, RagAnswer.Failure failure) {
        FlowRecorder flow = new FlowRecorder();
        flow.call(FlowRecorder.Actor.USER, FlowRecorder.Actor.APP, Msg.of("flow.question"), 0, 0, Msg.raw(question));
        return new RagAnswer(question, text,
                List.of(new RagAnswer.Source("T", "02-aerators.md", 2, "body", 1, 2, 0.03, 0.9, 0.9, 0.0, "kept", 1)),
                List.of(new RagAnswer.TraceLine("trace.step.total", Msg.of("trace.total", 10))), flow.hops(), "m:free", 10, failure, null, null);
    }

    @Test
    void turnsAreSavedAndTheAnswerComesBackWithItsTrace() {
        String id = history.saveTurn(null, answer("How do I reset AER-01 after error E-17?", "Hold RESET [1].", null));
        history.saveTurn(id, answer("And E-18?", null,
                new RagAnswer.Failure(ServiceStatus.Service.SYSTEM2, Msg.of("status.system2.allFailed", "a:free: busy"))));

        ChatSession session = history.sessions(0, 50).stream().filter(s -> s.getId().equals(id)).findFirst().orElseThrow();
        assertThat(session.getTitle()).isEqualTo("How do I reset AER-01 after error E-17?");
        assertThat(session.getMessageCount()).isEqualTo(4);

        List<ChatMessage> messages = history.messages(id);
        assertThat(messages).extracting(ChatMessage::getSender).containsExactly(ChatMessage.Sender.USER,
                ChatMessage.Sender.AI, ChatMessage.Sender.USER, ChatMessage.Sender.AI);
        assertThat(messages.get(3).getStatus()).isEqualTo(ChatMessage.Status.FAILED);
        assertThat(messages.get(3).getText()).isEqualTo("⚠️ SYSTEM2: status.system2.allFailed [a:free: busy]");

        RagAnswer back = ChatHistoryService.answerOf(messages.get(1));
        assertThat(back.text()).isEqualTo("Hold RESET [1].");
        assertThat(back.flow()).extracting(FlowRecorder.FlowHop::label).containsExactly(Msg.of("flow.question"));
        assertThat(back.failed()).isFalse();
        assertThat(ChatHistoryService.answerOf(messages.get(3)).failure().reason())
                .isEqualTo(Msg.of("status.system2.allFailed", "a:free: busy"));
        assertThat(back.sources().getFirst().used()).isTrue();
        assertThat(ChatHistoryService.answerOf(messages.getFirst())).isNull();
    }

    @Test
    void longQuestionsGiveAShortTitle() {
        assertThat(ChatHistoryService.title("word ".repeat(40))).hasSize(ChatHistoryService.TITLE_LENGTH).endsWith("…");
    }

    @Test
    void anAnswerSavedInAnOlderShapeIsShownAsTextNotAnError() {
        ChatMessage old = new ChatMessage("s", 2, ChatMessage.Sender.AI, ChatMessage.Status.OK, "old answer",
                "{\"trace\":[{\"step\":\"x\",\"detail\":\"a plain string, before Msg\"}],\"sources\":7}",
                Instant.now());
        assertThat(ChatHistoryService.answerOf(old)).isNull();
    }
}
