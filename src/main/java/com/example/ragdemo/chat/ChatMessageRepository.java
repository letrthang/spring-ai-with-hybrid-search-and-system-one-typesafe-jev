package com.example.ragdemo.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderBySeq(String sessionId);

    /** The newest messages of one sender (🛠 the agent's recent_questions tool reads the questions). */
    List<ChatMessage> findTop10BySenderOrderByCreatedAtDescIdDesc(ChatMessage.Sender sender);
}
