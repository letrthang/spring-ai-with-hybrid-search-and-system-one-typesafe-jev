package com.example.ragdemo.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordDialogTest {

    @Test
    void vectorPreviewShowsFirstAndLastThreeLikeTheConsole() {
        float[] v = {0.0101f, 0.0824f, 0.0343f, 0.5f, 0.6f, 0.0671f, -0.0093f, -0.0141f};
        assertThat(RecordDialog.shortPreview(v)).isEqualTo("[0.010, 0.082, 0.034, … , 0.067, -0.009, -0.014]");
        assertThat(RecordDialog.shortPreview(new float[] {1f, 2f})).isEqualTo("[1.000, 2.000]");
    }

    @Test
    void copyGivesEveryNumber() {
        assertThat(RecordDialog.full(new float[] {0.25f, -1f, 3.5f})).isEqualTo("[0.25, -1.0, 3.5]");
    }
}
