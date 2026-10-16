package com.example.ragdemo.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.time.Instant;

/**
 * One message: the user's question, or the AI's answer. An AI message keeps the whole {@code RagAnswer} as JSON
 * (sources, trace, data flow), so reopening a conversation shows exactly what happened.
 */
@Entity
public class ChatMessage {

    public enum Sender { USER, AI }

    public enum Status { OK, FAILED }

    @Id
    @GeneratedValue
    private Long id;

    private String sessionId;
    private int seq;
    private Sender sender;
    private Status status;

    @Lob
    @Column(name = "message_text")
    private String text;

    @Lob
    private String answerJson;

    private Instant createdAt;

    protected ChatMessage() {
    }

    ChatMessage(String sessionId, int seq, Sender sender, Status status, String text, String answerJson, Instant at) {
        this.sessionId = sessionId;
        this.seq = seq;
        this.sender = sender;
        this.status = status;
        this.text = text;
        this.answerJson = answerJson;
        this.createdAt = at;
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getSeq() {
        return seq;
    }

    public Sender getSender() {
        return sender;
    }

    public Status getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }

    public String getAnswerJson() {
        return answerJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
