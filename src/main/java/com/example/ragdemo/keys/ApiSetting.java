package com.example.ragdemo.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

/** One setting typed in Key Management: a name and its value (an API key, a URL, a model list). */
@Entity
public class ApiSetting {

    @Id
    private String name;

    @Column(name = "setting_value", length = 4000)   // "value" is a reserved word in H2
    private String value;

    private Instant updatedAt;

    protected ApiSetting() {
    }

    public ApiSetting(String name, String value) {
        this.name = name;
        this.value = value;
        this.updatedAt = Instant.now();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
