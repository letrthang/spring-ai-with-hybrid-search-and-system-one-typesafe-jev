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

    /** One row of a table in the dialog: a label, its value, and the lesson that explains it (may be null). */
    record Row(String label, String value, Hint hint) {
    }

    /** The DemoDoc fields: name, type, how Weaviate indexes it (i18n key), lesson. Kept in step with DemoDocCollection. */
    static final List<Row> SCHEMA = List.of(
            new Row("title", "text", Hint.BM25),
            new Row("body", "text", Hint.BM25),
            new Row("source", "text", Hint.SCHEMA),
            new Row("chunkNo", "int", Hint.SCHEMA),
            new Row("(vector)", "float[]", Hint.EMBEDDING));

    public RecordDialog(Hit hit, WeaviateRecords records, Component passage) {
        super("🔍 " + hit.title());
        Span loading = new Span(Tr.t("record.reading"));
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
                    body.add(new Span(Tr.t("record.gone")));
                } else {
                    body.add(where(stored, hit), structure(), vector(stored), raw(stored));
                }
            }, error -> loading.setText("⚠️ " + error.getMessage()));
        });
    }

    // ------------------------------------------------------------------ 📍 where it lives

    private static Div where(StoredRecord r, Hit hit) {
        Div rows = table(List.of(
                new Row(Tr.t("record.cluster"), r.cluster(), Hint.WEAVIATE),
                new Row(Tr.t("record.collection"), r.collection(), Hint.SCHEMA),
                new Row(Tr.t("record.uuid"), r.uuid(), Hint.RECORD),
                new Row(Tr.t("record.uuidHow"), Tr.t("record.uuidHow.value", hit.source() + "#" + hit.chunkNo()),
                        Hint.RECORD),
                new Row(Tr.t("record.file"), Tr.t("record.file.value", "src/main/resources/sample-data/" + hit.source(),
                        hit.chunkNo()), Hint.CHUNKS),
                new Row(Tr.t("record.times"), time(r.createdAt()) + " / " + time(r.updatedAt()), Hint.RECORD),
                new Row(Tr.t("record.rest"), r.cluster() + "/v1/objects/" + r.collection() + "/" + r.uuid(), Hint.RECORD)));
        return section(Tr.t("record.where"), Hint.RECORD, rows);
    }

    // ------------------------------------------------------------------ 🧱 data structure

    private static Div structure() {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row(Tr.t("record.field"), Tr.t("record.fieldHow"), null));
        for (Row f : SCHEMA) rows.add(new Row(f.label(), f.value() + " · " + Tr.t("record.index." + f.label()), f.hint()));
        Div table = table(rows);
        table.getChildren().findFirst().ifPresent(header -> header.addClassName("font-semibold"));
        return section(Tr.t("record.structure", DemoDocCollection.NAME), Hint.SCHEMA, table);
    }

    // ------------------------------------------------------------------ 🧮 vector

    private static Div vector(StoredRecord r) {
        String text = r.vector() == null ? Tr.t("record.noVector")
                : Tr.t("record.vectorText", r.vector().length, preview(r.vector(), 8));
        Span span = new Span(text);
        span.addClassNames("text-sm", "break-all");
        return section(Tr.t("record.vector"), Hint.EMBEDDING, span);
    }

    // ------------------------------------------------------------------ { } raw record

    private static Div raw(StoredRecord r) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("class", r.collection());
        json.put("id", r.uuid());
        json.put("properties", r.properties());
        json.put("creationTimeUnix", r.createdAt());
        json.put("lastUpdateTimeUnix", r.updatedAt());
        json.put("vector", r.vector() == null ? null : "[" + preview(r.vector(), 4) + ", … " + r.vector().length + "]");
        Pre pre = new Pre(JSON.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        pre.addClassNames("rd-json", "m-0");
        return section(Tr.t("record.raw"), Hint.RECORD, pre);
    }

    // ------------------------------------------------------------------ helpers

    private static Div section(String title, Hint hint, Component content) {
        H3 heading = new H3(title);
        heading.addClassNames("m-0", "text-sm");
        Div section = new Div(InfoHint.with(heading, hint), content);
        section.addClassNames("rd-record-section", "flex", "flex-col", "gap-2", "p-3");
        return section;
    }

    private static Div table(List<Row> rows) {
        Div table = new Div();
        table.addClassNames("rd-record-table", "text-sm");
        for (Row row : rows) {
            Span keyText = new Span(row.label());
            keyText.addClassNames("rd-muted");
            Div key = row.hint() == null ? new Div(keyText) : InfoHint.with(keyText, row.hint());
            Span value = new Span(row.value());
            value.addClassNames("break-all");
            Div line = new Div(key, value);
            line.addClassName("rd-record-row");
            table.add(line);
        }
        return table;
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
