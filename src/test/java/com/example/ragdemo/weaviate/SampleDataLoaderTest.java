package com.example.ragdemo.weaviate;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SampleDataLoaderTest {

    @Test
    void splitsAtSectionHeadingsAndPrefixesTheDocumentTitle() {
        List<SampleDataLoader.Chunk> chunks = SampleDataLoader.split("x.md", """
                # Pond guide

                intro text before any section is not a passage
                ## Oxygen
                Keep DO above 5 mg/L.
                ## Empty section
                ## pH
                Keep pH between 6.5 and 8.5.
                """);

        assertThat(chunks).extracting(SampleDataLoader.Chunk::title)
                .containsExactly("Pond guide — Oxygen", "Pond guide — pH");
        assertThat(chunks.getFirst().body()).isEqualTo("Keep DO above 5 mg/L.");
        assertThat(chunks).extracting(SampleDataLoader.Chunk::chunkNo).containsExactly(1, 2);
    }

    @Test
    void sampleDataHasTheDemoPassagesWithStableIds() {
        List<SampleDataLoader.Chunk> chunks = SampleDataLoader.readSampleData();

        assertThat(chunks).hasSizeGreaterThan(15);
        assertThat(chunks).anyMatch(c -> c.body().contains("AER-01"));                      // a BM25 win
        assertThat(chunks).anyMatch(c -> c.body().contains("Ignore all previous instructions"));   // for System 1
        assertThat(chunks).anyMatch(c -> c.title().contains("Canteen menu"));                // off-topic
        assertThat(chunks).extracting(SampleDataLoader.Chunk::id).doesNotHaveDuplicates();
        assertThat(SampleDataLoader.readSampleData().getFirst().id()).isEqualTo(chunks.getFirst().id());
    }

    @Test
    void wrappedLinesBecomeOneParagraphButListsAndBlankLinesStay() {
        assertThat(SampleDataLoader.unwrap("one\ntwo\n\nthree\n- a\n  continued\n- b"))
                .isEqualTo("one two\n\nthree\n- a continued\n- b");
    }
}
