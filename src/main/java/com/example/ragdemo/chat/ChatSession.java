package com.example.ragdemo.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

/** One conversation in the 📂 History grid. */
@Entity
public class ChatSession {

    @Id
    private String id;

    private String title;
    private Instant startedAt;
    private Instant lastMessageAt;
    private int messageCount;

    protected ChatSession() {
    }

    ChatSession(String id, String title, Instant now) {
        this.id = id;
        this.title = title;
        this.startedAt = now;
        this.lastMessageAt = now;
    }

    void addMessages(int count, Instant at) {
        messageCount += count;
        lastMessageAt = at;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public int getMessageCount() {
        return messageCount;
    }
}
