package com.example.ragdemo.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlingTest {

    @Test
    void aBrowserThatWentAwayIsNotAnError() {
        assertThat(ErrorHandling.clientGone(new IOException("Broken pipe"))).isTrue();
        assertThat(ErrorHandling.clientGone(new UncheckedIOException(new IOException("Connection reset by peer")))).isTrue();
    }

    @Test
    void realFailuresStillCount() {
        assertThat(ErrorHandling.clientGone(new IllegalStateException("boom"))).isFalse();
        assertThat(ErrorHandling.clientGone(new IOException("disk full"))).isFalse();
    }
}
