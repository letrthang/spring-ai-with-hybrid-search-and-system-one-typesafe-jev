package com.example.ragdemo.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppVersionTest {

    @Test
    void theVersionComesFromTheBuild_andLinksToItsReleaseNotes() {
        assertThat(AppVersion.VERSION).matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?");   // pom.xml → build-info.properties
        assertThat(AppVersion.notesUrl()).startsWith(AppVersion.GITHUB + "/releases");
        if (AppVersion.released()) assertThat(AppVersion.notesUrl()).endsWith("/releases/tag/v" + AppVersion.VERSION);
    }
}
