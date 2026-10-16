package com.example.ragdemo.ui;

import com.example.ragdemo.weaviate.HybridRetriever.Hit;
import com.example.ragdemo.weaviate.DemoDocCollection;
import com.example.ragdemo.weaviate.WeaviateRecords;
import com.example.ragdemo.weaviate.WeaviateRecords.StoredRecord;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 🔍 Details of one passage (double-click in 🔎 Search Data): the passage, <b>where the record lives</b> (cluster →
 * collection → object UUID and how that UUID is made), the <b>data structure</b> of the collection, the <b>vector</b>
 * Weaviate stored, and the <b>raw record</b> as JSON — read live from Weaviate.
 */
public class RecordDialog extends AppDialog {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** The DemoDoc fields: name, type, how Weaviate indexes it. Kept in step with DemoDocCollection. */
    static final List<String[]> SCHEMA = List.of(
            new String[]{"title", "text", "word tokens → BM25 (weight ×2) · part of the vector"},
            new String[]{"body", "text", "word tokens → BM25 · part of the vector"},
            new String[]{"source", "text", "whole value (field) · filter only, not searched"},
            new String[]{"chunkNo", "int", "filter only"},
            new String[]{"(vector)", "float[]", "text2vec-weaviate over title + body · HFresh index · cosine"});

    public RecordDialog(Hit hit, WeaviateRecords records, Component passage) {
        super("🔍 " + hit.title());
        Span loading = new Span("⏳ Reading the record from Weaviate…");
        loading.addClassNames("rd-muted", "text-sm");
        Div body = new Div(passage, loading);
        body.addClassNames("flex", "flex-col", "gap-4");
        add(body);
        // on attach, not on "opened": background work needs the content attached to the UI
        body.addAttachListener(e -> {
            if (!e.isInitialAttach()) return;
            Background.run(body, () -> records.fetch(hit.id()), stored -> {
                body.remove(loading);
                if (stored == null) {
                    body.add(new Span("This record no longer exists in Weaviate."));
                } else {
                    body.add(where(stored, hit), structure(), vector(stored), raw(stored));
                }
            }, error -> loading.setText("⚠️ " + error.getMessage()));
        });
    }

    // ------------------------------------------------------------------ 📍 where it lives

    private static Div where(StoredRecord r, Hit hit) {
        Div rows = table(List.of(
                new String[]{"Cluster", r.cluster()},
                new String[]{"Collection", r.collection()},
                new String[]{"Object UUID (key)", r.uuid()},
                new String[]{"How the UUID is made",
                        "UUID.nameUUIDFromBytes(\"" + hit.source() + "#" + hit.chunkNo() + "\") — fixed, from file + section"},
                new String[]{"From file", "src/main/resources/sample-data/" + hit.source() + " · section " + hit.chunkNo()},
                new String[]{"Created / updated", time(r.createdAt()) + " / " + time(r.updatedAt())},
                new String[]{"REST address", r.cluster() + "/v1/objects/" + r.collection() + "/" + r.uuid()}));
        return section("📍 Where the record lives", Hint.RECORD, rows);
    }

    // ------------------------------------------------------------------ 🧱 data structure

    private static Div structure() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Field", "Type · how it is indexed"});
        for (String[] f : SCHEMA) rows.add(new String[]{f[0], f[1] + " · " + f[2]});
        Div table = table(rows);
        table.getChildren().findFirst().ifPresent(header -> header.addClassName("font-semibold"));
        return section("🧱 Data structure — collection " + DemoDocCollection.NAME, Hint.SCHEMA, table);
    }

    // ------------------------------------------------------------------ 🧮 vector

    private static Div vector(StoredRecord r) {
        String text = r.vector() == null ? "No vector returned."
                : r.vector().length + " numbers (dimensions), made by Weaviate from title + body. First 8: "
                + preview(r.vector(), 8);
        Span span = new Span(text);
        span.addClassNames("text-sm", "break-all");
        return section("🧮 Vector", Hint.EMBEDDING, span);
    }

    // ------------------------------------------------------------------ { } raw record

    private static Div raw(StoredRecord r) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("class", r.collection());
        json.put("id", r.uuid());
        json.put("properties", r.properties());
        json.put("creationTimeUnix", r.createdAt());
        json.put("lastUpdateTimeUnix", r.updatedAt());
        json.put("vector", r.vector() == null ? null : "[" + preview(r.vector(), 4) + ", … " + r.vector().length + " numbers]");
        Pre pre = new Pre(JSON.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        pre.addClassNames("rd-json", "m-0");
        return section("{ } Raw record (as stored in Weaviate)", Hint.RECORD, pre);
    }

    // ------------------------------------------------------------------ helpers

    private static Div section(String title, Hint hint, Component content) {
        H3 heading = new H3(title);
        heading.addClassNames("m-0", "text-sm");
        Div section = new Div(InfoHint.with(heading, hint), content);
        section.addClassNames("rd-record-section", "flex", "flex-col", "gap-2", "p-3");
        return section;
    }

    private static Div table(List<String[]> rows) {
        Div table = new Div();
        table.addClassNames("rd-record-table", "text-sm");
        for (String[] row : rows) {
            Span keyText = new Span(row[0]);
            keyText.addClassNames("rd-muted");
            Hint hint = hintFor(row[0]);
            Div key = hint == null ? new Div(keyText) : InfoHint.with(keyText, hint);
            Span value = new Span(row[1]);
            value.addClassNames("break-all");
            Div line = new Div(key, value);
            line.addClassName("rd-record-row");
            table.add(line);
        }
        return table;
    }

    /** The lesson for a row of the record dialog, by its label. */
    static Hint hintFor(String key) {
        return switch (key) {
            case "Cluster" -> Hint.WEAVIATE;
            case "Collection", "source", "chunkNo" -> Hint.SCHEMA;
            case "Object UUID (key)", "How the UUID is made", "Created / updated", "REST address" -> Hint.RECORD;
            case "From file" -> Hint.CHUNKS;
            case "title", "body" -> Hint.BM25;
            case "(vector)" -> Hint.EMBEDDING;
            default -> null;
        };
    }

    private static String preview(float[] v, int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < Math.min(n, v.length); i++) {
            if (i > 0) s.append(", ");
            s.append(String.format(Locale.ROOT, "%.4f", v[i]));
        }
        return s.toString();
    }

    private static String time(Long millis) {
        return millis == null ? "—" : Instant.ofEpochMilli(millis).toString();
    }
}
