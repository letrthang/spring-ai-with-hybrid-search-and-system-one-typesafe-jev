package com.example.ragdemo.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BackgroundTest {

    @Test
    void anErrorWithoutAMessageStillReadsWell() {
        assertThat(Background.message(new NullPointerException())).isEqualTo("NullPointerException");
        assertThat(Background.message(new IllegalStateException(" "))).isEqualTo("IllegalStateException");
        assertThat(Background.message(new IllegalStateException("boom"))).isEqualTo("boom");
    }
}
