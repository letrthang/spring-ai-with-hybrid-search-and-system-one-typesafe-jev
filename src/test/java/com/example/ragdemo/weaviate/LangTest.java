package com.example.ragdemo.weaviate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LangTest {

    @Test
    void detectsTheThreeLanguagesByScript() {
        assertThat(Lang.of("What does PCR do?")).isEqualTo(Lang.EN);
        assertThat(Lang.of("PCR dùng để làm gì?")).isEqualTo(Lang.VI);
        assertThat(Lang.of("Định luật Ohm nói gì?")).isEqualTo(Lang.VI);
        assertThat(Lang.of("PCR は何をするもの？")).isEqualTo(Lang.JA);
        assertThat(Lang.of("温かい水")).isEqualTo(Lang.JA);   // kanji only
        assertThat(Lang.of(null)).isEqualTo(Lang.EN);
    }
}
