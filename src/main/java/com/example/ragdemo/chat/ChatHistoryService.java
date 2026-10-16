package com.example.ragdemo.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.ragdemo.rag.RagAnswer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Saves each question + answer to the H2 file DB and reads the 📂 History back. */
@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);

    static final int TITLE_LENGTH = 80;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final ChatSessionRepository sessions;
    private final ChatMessageRepository messages;

    public ChatHistoryService(ChatSessionRepository sessions, ChatMessageRepository messages) {
        this.sessions = sessions;
        this.messages = messages;
    }

    /**
     * Saves one turn: the question and the answer (or the failure). {@code sessionId} null starts a new conversation
     * titled after the question. Returns the conversation id.
     */
    @Transactional
    public String saveTurn(String sessionId, RagAnswer answer) {
        Instant now = Instant.now();
        ChatSession session = sessionId == null ? null : sessions.findById(sessionId).orElse(null);
        if (session == null) {
            session = new ChatSession(UUID.randomUUID().toString(), title(answer.question()), now);
        }
        int seq = session.getMessageCount();
        messages.save(new ChatMessage(session.getId(), seq + 1, ChatMessage.Sender.USER, ChatMessage.Status.OK,
                answer.question(), null, now));
        String aiText = answer.failed() ? "⚠️ " + answer.failure().service().label + ": " + answer.failure().reason()
                : answer.text();
        messages.save(new ChatMessage(session.getId(), seq + 2, ChatMessage.Sender.AI,
                answer.failed() ? ChatMessage.Status.FAILED : ChatMessage.Status.OK, aiText,
                JSON.writeValueAsString(answer), now));
        session.addMessages(2, now);
        sessions.save(session);
        log.debug("💾 saved turn {} of conversation {} ({})", session.getMessageCount() / 2, session.getId(),
                answer.failed() ? "FAILED" : "OK");
        return session.getId();
    }

    /** A page of conversations, most recent first. */
    public List<ChatSession> sessions(int offset, int limit) {
        int page = offset / Math.max(1, limit);
        return sessions.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "lastMessageAt")))
                .getContent();
    }

    public long countSessions() {
        return sessions.count();
    }

    public List<ChatMessage> messages(String sessionId) {
        return messages.findBySessionIdOrderBySeq(sessionId);
    }

    /** The saved RagAnswer of an AI message (null for a user message). */
    public static RagAnswer answerOf(ChatMessage message) {
        return message.getAnswerJson() == null ? null : JSON.readValue(message.getAnswerJson(), RagAnswer.class);
    }

    static String title(String question) {
        String oneLine = question.replaceAll("\\s+", " ").strip();
        return oneLine.length() <= TITLE_LENGTH ? oneLine : oneLine.substring(0, TITLE_LENGTH - 1) + "…";
    }
}
