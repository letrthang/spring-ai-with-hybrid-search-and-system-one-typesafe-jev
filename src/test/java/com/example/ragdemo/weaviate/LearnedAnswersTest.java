package com.example.ragdemo.weaviate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LearnedAnswersTest {

    @Test
    void caseSpacesAndEndPunctuationDoNotMakeANewQuestion() {
        assertThat(LearnedAnswers.sameQuestion("What does a RAS system do?", "  what does a  ras system do ")).isTrue();
        assertThat(LearnedAnswers.sameQuestion("RAS とは何ですか？", "RAS とは何ですか")).isTrue();
        assertThat(LearnedAnswers.sameQuestion("What does a RAS system do?", "What is a RAS?")).isFalse();
        // so asking it again replaces the stored answer instead of adding a second one
        assertThat(LearnedAnswers.idOf("en", "What does a RAS system do?"))
                .isEqualTo(LearnedAnswers.idOf("en", "what does a ras system do"))
                .isNotEqualTo(LearnedAnswers.idOf("vi", "what does a ras system do"));
    }
}
