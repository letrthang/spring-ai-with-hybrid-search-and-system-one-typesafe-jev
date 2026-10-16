package com.example.ragdemo.ui;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/** All three languages must have exactly the same keys, and every ℹ️ lesson must be complete. */
class TranslationsTest {

    static Properties load(String file) throws Exception {
        Properties p = new Properties();
        try (InputStream in = TranslationsTest.class.getResourceAsStream("/vaadin-i18n/" + file)) {
            assertThat(in).as(file).isNotNull();
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }

    @Test
    void vietnameseAndJapaneseHaveEveryEnglishKey() throws Exception {
        Set<String> en = new TreeSet<>(load("translations.properties").stringPropertyNames());
        for (String file : new String[]{"translations_vi.properties", "translations_ja.properties"}) {
            Set<String> other = new TreeSet<>(load(file).stringPropertyNames());
            Set<String> missing = new TreeSet<>(en);
            missing.removeAll(other);
            Set<String> extra = new TreeSet<>(other);
            extra.removeAll(en);
            assertThat(missing).as("missing in " + file).isEmpty();
            assertThat(extra).as("unknown keys in " + file).isEmpty();
        }
    }

    @Test
    void everyLessonHasAllItsTextsInEveryLanguage() {
        for (var locale : Tr.LANGUAGES) {
            for (Hint h : Hint.values()) {
                for (String part : new String[]{"name", "title", "what", "does", "theory"}) {
                    assertThat(Tr.t(locale, "hint." + h.name() + "." + part)).as(locale + " " + h + " " + part)
                            .doesNotStartWith("!").isNotBlank();
                }
            }
        }
    }

    @Test
    void argumentsAndTranslatedArguments() {
        assertThat(Tr.t(Tr.ENGLISH, "answer.sources", 2, 4)).isEqualTo("📚 Sources — 2 used of 4");
        assertThat(Tr.t(Tr.ENGLISH, "flow.stopped", "@service.WEAVIATE")).isEqualTo("⛔ stopped: 🗄️ Weaviate");
        assertThat(Tr.t(Tr.ENGLISH, "no.such.key")).isEqualTo("!no.such.key");
    }
}
