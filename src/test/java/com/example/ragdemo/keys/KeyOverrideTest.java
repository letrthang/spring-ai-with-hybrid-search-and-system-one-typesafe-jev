package com.example.ragdemo.keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keys set by the owner as environment variables (on Render: RAGDEMO_WEAVIATE_URL / _KEY, RAGDEMO_OPENROUTER_KEY) are
 * only defaults: a key a visitor saves in 🔑 Key Management wins, and 🗑 Clear / 🔄 Defaults brings the owner's back.
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:override;DB_CLOSE_DELAY=-1",
        "ragdemo.defaults.weaviate.url=https://owner.weaviate.cloud",
        "ragdemo.defaults.weaviate.key=owner-weaviate-key",
        "ragdemo.defaults.system2.key=sk-or-v1-owner"})
class KeyOverrideTest {

    @Autowired
    KeyStore keys;

    @Autowired
    ApiSettingRepository repository;

    @BeforeEach
    void empty() {
        repository.deleteAll();
        keys.clearTyped();
    }

    @Test
    void ownersEnvironmentKeysAreTheDefaults() {
        assertThat(keys.weaviateUrl()).isEqualTo("https://owner.weaviate.cloud");
        assertThat(keys.weaviateKey()).isEqualTo("owner-weaviate-key");
        assertThat(keys.system2Key()).isEqualTo("sk-or-v1-owner");
        assertThat(keys.fromConfig(KeyStore.WEAVIATE_KEY)).isTrue();
        assertThat(keys.fromConfig(KeyStore.SYSTEM2_KEY)).isTrue();
    }

    @Test
    void aKeySavedInKeyManagementWinsOverTheEnvironment() {
        keys.set(KeyStore.WEAVIATE_URL, "https://visitor.weaviate.cloud");
        keys.set(KeyStore.WEAVIATE_KEY, "visitor-weaviate-key");
        keys.set(KeyStore.SYSTEM2_KEY, "sk-or-v1-visitor");

        assertThat(keys.weaviateUrl()).isEqualTo("https://visitor.weaviate.cloud");
        assertThat(keys.weaviateKey()).isEqualTo("visitor-weaviate-key");
        assertThat(keys.system2Key()).isEqualTo("sk-or-v1-visitor");
        assertThat(keys.fromConfig(KeyStore.SYSTEM2_KEY)).isFalse();
    }

    @Test
    void clearingTheSavedKeyGoesBackToTheEnvironmentKey() {
        keys.set(KeyStore.SYSTEM2_KEY, "sk-or-v1-visitor");
        keys.set(KeyStore.SYSTEM2_KEY, "");   // 🗑 Clear / 🔄 Defaults
        assertThat(keys.system2Key()).isEqualTo("sk-or-v1-owner");
    }
}
