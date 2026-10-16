package com.example.ragdemo.weaviate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeaviateSchemaTest {

    /** The shape Weaviate returns for GET /v1/schema/DemoDoc with a named vector (shortened). */
    static final String SCHEMA = """
            {"class": "DemoDoc", "description": "RAG demo passages",
             "properties": [
               {"name": "title", "dataType": ["text"], "tokenization": "word", "indexSearchable": true, "indexFilterable": true},
               {"name": "chunkNo", "dataType": ["int"], "indexSearchable": false, "indexFilterable": true},
               {"name": "lang", "dataType": ["text"], "tokenization": "field", "indexSearchable": false, "indexFilterable": true}],
             "vectorConfig": {"default": {"vectorIndexType": "hfresh",
                "vectorizer": {"text2vec-weaviate": {"sourceProperties": ["title", "body"]}}}}}
            """;

    @Test
    void readsTheCollectionVectorizerIndexAndEveryProperty() {
        var s = WeaviateSchema.parse("https://x/v1/schema/DemoDoc", SCHEMA);
        assertThat(s.collection()).isEqualTo("DemoDoc");
        assertThat(s.vectorizer()).isEqualTo("text2vec-weaviate");
        assertThat(s.vectorIndex()).isEqualTo("hfresh");
        assertThat(s.vectorSources()).containsExactly("title", "body");
        assertThat(s.fields()).extracting(WeaviateSchema.Field::name).containsExactly("title", "chunkNo", "lang");
        assertThat(s.fields().get(1).tokenization()).isEqualTo("—");
        assertThat(s.fields().get(2).filterable()).isTrue();
        assertThat(s.json()).contains("\"vectorConfig\"");
    }
}
