package com.example.ragdemo.chat;

import com.example.ragdemo.rag.FlowRecorder;
import com.example.ragdemo.rag.RagAnswer;
import com.example.ragdemo.rag.ServiceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:history;DB_CLOSE_DELAY=-1")
class ChatHistoryServiceTest {

    @Autowired
    ChatHistoryService history;

    static RagAnswer answer(String question, String text, RagAnswer.Failure failure) {
        FlowRecorder flow = new FlowRecorder();
        flow.call(FlowRecorder.Actor.USER, FlowRecorder.Actor.APP, "1. question", 0, 0, question);
        return new RagAnswer(question, text,
                List.of(new RagAnswer.Source("T", "02-aerators.md", 2, "body", 1, 2, 0.03, 0.9, 0.9, 0.0, "kept", 1)),
                List.of(new RagAnswer.TraceLine("⏱️ Total", "10 ms")), flow.hops(), "m:free", 10, failure);
    }

    @Test
    void turnsAreSavedAndTheAnswerComesBackWithItsTrace() {
        String id = history.saveTurn(null, answer("How do I reset AER-01 after error E-17?", "Hold RESET [1].", null));
        history.saveTurn(id, answer("And E-18?", null,
                new RagAnswer.Failure(ServiceStatus.Service.SYSTEM2, "every model failed")));

        ChatSession session = history.sessions(0, 50).stream().filter(s -> s.getId().equals(id)).findFirst().orElseThrow();
        assertThat(session.getTitle()).isEqualTo("How do I reset AER-01 after error E-17?");
        assertThat(session.getMessageCount()).isEqualTo(4);

        List<ChatMessage> messages = history.messages(id);
        assertThat(messages).extracting(ChatMessage::getSender).containsExactly(ChatMessage.Sender.USER,
                ChatMessage.Sender.AI, ChatMessage.Sender.USER, ChatMessage.Sender.AI);
        assertThat(messages.get(3).getStatus()).isEqualTo(ChatMessage.Status.FAILED);
        assertThat(messages.get(3).getText()).isEqualTo("⚠️ 🧠 System 2: every model failed");

        RagAnswer back = ChatHistoryService.answerOf(messages.get(1));
        assertThat(back.text()).isEqualTo("Hold RESET [1].");
        assertThat(back.flow()).extracting(FlowRecorder.FlowHop::label).containsExactly("1. question");
        assertThat(back.sources().getFirst().used()).isTrue();
        assertThat(ChatHistoryService.answerOf(messages.getFirst())).isNull();
    }

    @Test
    void longQuestionsGiveAShortTitle() {
        assertThat(ChatHistoryService.title("word ".repeat(40))).hasSize(ChatHistoryService.TITLE_LENGTH).endsWith("…");
    }
}
