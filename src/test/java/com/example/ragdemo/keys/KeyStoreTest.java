package com.example.ragdemo.keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:keys;DB_CLOSE_DELAY=-1")
class KeyStoreTest {

    @Autowired
    KeyStore keys;

    @Autowired
    ApiSettingRepository repository;

    @BeforeEach
    void empty() {
        repository.deleteAll();
    }

    @Test
    void defaultsAreFreeModelsOpenRouterAndPublicLaya() {
        assertThat(keys.system2Models()).isNotEmpty().allMatch(m -> m.endsWith(":free"));
        assertThat(keys.system2BaseUrl()).isEqualTo("https://openrouter.ai/api/v1");
        assertThat(keys.system1Url()).isEqualTo(KeyStore.DEFAULT_SYSTEM1_URL);
        assertThat(keys.system2Key()).isEmpty();
        assertThat(keys.weaviateKey()).isEmpty();
    }

    @Test
    void savedValuesAreReadBackAndBlankRemovesThem() {
        keys.set(KeyStore.SYSTEM2_KEY, "  sk-or-v1-abcdefgh1234 ");
        keys.set(KeyStore.WEAVIATE_URL, "https://demo.weaviate.cloud/");
        assertThat(keys.system2Key()).isEqualTo("sk-or-v1-abcdefgh1234");
        assertThat(keys.weaviateUrl()).isEqualTo("https://demo.weaviate.cloud");

        keys.set(KeyStore.SYSTEM2_KEY, " ");
        assertThat(keys.has(KeyStore.SYSTEM2_KEY)).isFalse();
    }

    @Test
    void modelsOnePerLineIgnoringBlanks() {
        keys.set(KeyStore.SYSTEM2_MODELS, "a/one:free\n\n  b/two:free  \n");
        assertThat(keys.system2Models()).isEqualTo(List.of("a/one:free", "b/two:free"));
    }

    @Test
    void maskShowsOnlyTheLastFourCharacters() {
        assertThat(KeyStore.mask("sk-or-v1-abcdefgh1234")).isEqualTo("••••1234");
        assertThat(KeyStore.mask("short")).isEqualTo("••••");
        assertThat(KeyStore.mask("")).isEmpty();
    }
}
