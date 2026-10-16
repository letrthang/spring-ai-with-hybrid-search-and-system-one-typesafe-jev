package com.example.ragdemo.ui;

import com.example.ragdemo.weaviate.HybridRetriever.Hit;
import com.example.ragdemo.weaviate.DemoDocCollection;
import com.example.ragdemo.weaviate.WeaviateRecords;
import com.example.ragdemo.weaviate.WeaviateRecords.StoredRecord;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 🔍 Details of one passage (double-click in 🔎 Search Data): the passage, then the <b>stored object as the Weaviate
 * console shows it</b> (metadata · properties · vectors), <b>where the record lives</b> (cluster → collection → object
 * UUID and how that UUID is made) and the <b>data structure</b> of the collection — read live from Weaviate.
 */
public class RecordDialog extends AppDialog {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    /** Pretty JSON like the console: arrays one item per line, "key": value. */
    private static final DefaultPrettyPrinter PRETTY = new DefaultPrettyPrinter(Separators.createDefaultInstance()
            .withObjectNameValueSpacing(Separators.Spacing.AFTER))
            .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

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
        setFixedHeight("min(85vh, 860px)");   // the record is read in the background: never resize when it lands
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
                    body.add(object(stored), where(stored, hit), structure());
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

    // ------------------------------------------------------------------ 🧱 schema of one record (the detail card)

    /**
     * For the detail card next to the grid: every DemoDoc field of this record — its name, type, how Weaviate indexes
     * it, and this record's value. Built from the search hit, so it needs no extra call to Weaviate.
     */
    static Div schemaOf(Hit hit) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("title", hit.title());
        values.put("body", shorten(hit.body(), 90));
        values.put("source", hit.source());
        values.put("chunkNo", hit.chunkNo() == null ? "—" : String.valueOf(hit.chunkNo()));
        values.put("(vector)", Tr.t("record.vectorInCard"));
        Div rows = new Div();
        rows.addClassNames("flex", "flex-col", "gap-2");
        for (Row f : SCHEMA) {
            Span type = new Span(f.value() + " · " + Tr.t("record.index." + f.label()));
            type.addClassNames("rd-muted", "text-xs");
            Div line = field(f.label(), values.get(f.label()));
            rows.add(new Div(InfoHint.with(line, f.hint()), type));
        }
        H3 heading = new H3(Tr.t("record.structure", DemoDocCollection.NAME));
        heading.addClassNames("m-0", "text-sm");
        Div box = new Div(InfoHint.with(heading, Hint.SCHEMA), rows);
        box.addClassNames("rd-wv", "rd-record-section", "p-3");
        return box;
    }

    private static String shorten(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max).strip() + "…";
    }

    // ------------------------------------------------------------------ 🗂️ the object, as Weaviate Explorer shows it

    /**
     * The stored object laid out like the Weaviate Cloud console (Explorer → Browse): an "ID uuid 📋" line, then the
     * object as <b>JSON</b> (the console's default) or, after "Show fields", as <b>fields</b> — {@code metadata:}
     * (folded), {@code properties:} as key chips + value boxes, {@code vectors:} with the size, Copy and a short
     * preview. The field names stay as Weaviate writes them, so people recognise them in the console.
     */
    private static Div object(StoredRecord r) {
        Div json = json(r);
        Div fields = fields(r);
        fields.setVisible(false);
        Button toggle = new Button(Tr.t("record.showFields"));
        toggle.addThemeVariants(ButtonVariant.SMALL);
        toggle.addClickListener(e -> {
            boolean showFields = !fields.isVisible();
            fields.setVisible(showFields);
            json.setVisible(!showFields);
            toggle.setText(Tr.t(showFields ? "record.showJson" : "record.showFields"));
        });
        Span idLabel = new Span("ID");
        idLabel.addClassName("rd-muted");
        Span id = new Span(r.uuid());
        id.addClassName("rd-wv-mono");
        Button copyId = copyButton("📋", r.uuid());
        copyId.setTooltipText(Tr.t("record.copy"));
        Div idLine = new Div(idLabel, id, copyId);
        idLine.addClassNames("flex", "items-center", "gap-2", "min-w-0", "flex-wrap");
        Div head = new Div(idLine, toggle);
        head.addClassNames("flex", "items-center", "justify-between", "gap-2", "flex-wrap");
        Div card = new Div(head, json, fields);
        card.addClassName("rd-wv");
        return section(Tr.t("record.object"), Hint.RECORD, card);
    }

    /** The JSON the console shows: uuid, metadata, properties, vectors (every number, one per line). */
    private static Div json(StoredRecord r) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("creationTime", time(r.createdAt()));
        metadata.put("lastUpdateTime", time(r.updatedAt()));
        Map<String, Object> object = new LinkedHashMap<>();
        object.put("uuid", r.uuid());
        object.put("metadata", metadata);
        object.put("properties", ordered(r.properties()));
        object.put("vectors", r.vector() == null ? Map.of() : Map.of("default", r.vector()));
        Pre pre = new Pre(JSON.writer().with(PRETTY).writeValueAsString(object));
        pre.addClassNames("rd-wv-json", "m-0");
        return new Div(pre);
    }

    private static Div fields(StoredRecord r) {
        Div metadata = new Div(
                field("creationTime", time(r.createdAt())),
                field("lastUpdateTime", time(r.updatedAt())));
        Div properties = new Div();
        for (Map.Entry<String, Object> e : ordered(r.properties()).entrySet()) {
            properties.add(field(e.getKey(), value(e.getValue())));
        }
        Div box = new Div(fold("metadata:", metadata, false), group("properties:"), indent(properties),
                fold("vectors:", vectors(r.vector()), true));
        box.addClassNames("flex", "flex-col", "gap-2");
        return box;
    }

    /** A button that puts {@code text} on the clipboard and says so. */
    private static Button copyButton(String label, String text) {
        Button copy = new Button(label);
        copy.addThemeVariants(ButtonVariant.SMALL, ButtonVariant.TERTIARY);
        copy.addClickListener(e -> copy.getUI().ifPresent(ui -> ui.getPage()
                .executeJs("return navigator.clipboard.writeText($0)", text)
                .then(ok -> Notification.show(Tr.t("record.copied"), 1500, Notification.Position.BOTTOM_CENTER),
                        error -> Notification.show(Tr.t("record.copyFailed"), 3000,
                                Notification.Position.BOTTOM_CENTER))));
        return copy;
    }

    private static Component vectors(float[] v) {
        if (v == null) return new Span(Tr.t("record.noVector"));
        Span name = new Span("default (" + v.length + "d)");
        name.addClassName("rd-muted");
        Div head = new Div(name, copyButton(Tr.t("record.copy"), full(v)));
        head.addClassNames("flex", "items-center", "gap-2");
        Span preview = new Span(shortPreview(v));
        preview.addClassName("rd-wv-vector");
        Div box = new Div(head, preview);
        box.addClassName("rd-wv-line");
        return box;
    }

    /** "metadata:" / "vectors:" — a purple chip that folds its content in and out, like the console. */
    private static Details fold(String label, Component content, boolean opened) {
        Details details = new Details(group(label), indent(content));
        details.setOpened(opened);
        details.addClassName("rd-wv-fold");
        return details;
    }

    private static Span group(String label) {
        Span chip = new Span(label);
        chip.addClassName("rd-wv-group");
        return chip;
    }

    private static Div indent(Component content) {
        Div box = new Div(content);
        box.addClassName("rd-wv-indent");
        return box;
    }

    /** One "key: value" line — grey key chip, bordered value box. */
    private static Div field(String key, String value) {
        Span k = new Span(key + ":");
        k.addClassName("rd-wv-key");
        Span v = new Span(value);
        v.addClassName("rd-wv-value");
        Div line = new Div(k, v);
        line.addClassName("rd-wv-field");
        return line;
    }

    /** The DemoDoc fields in schema order (title, body, source, chunkNo), then anything else Weaviate returned. */
    private static Map<String, Object> ordered(Map<String, Object> props) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Row f : SCHEMA) if (props.containsKey(f.label())) out.put(f.label(), props.get(f.label()));
        props.forEach(out::putIfAbsent);
        return out;
    }

    /** 3.0 → "3": Weaviate returns ints as numbers that may come back as doubles. */
    private static String value(Object o) {
        if (o instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())) return String.valueOf(n.longValue());
        return String.valueOf(o);
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

    /** [0.010, 0.082, 0.034, … , 0.067, -0.009, -0.014] — the first and last three numbers, like the console. */
    static String shortPreview(float[] v) {
        if (v.length <= 6) return "[" + join(v, 0, v.length, "%.3f") + "]";
        return "[" + join(v, 0, 3, "%.3f") + ", … , " + join(v, v.length - 3, v.length, "%.3f") + "]";
    }

    /** Every number, for the Copy button. */
    static String full(float[] v) {
        return "[" + join(v, 0, v.length, "%s") + "]";
    }

    private static String join(float[] v, int from, int to, String format) {
        StringBuilder s = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) s.append(", ");
            s.append(format.equals("%s") ? Float.toString(v[i]) : String.format(Locale.ROOT, format, v[i]));
        }
        return s.toString();
    }

    private static String time(Long millis) {
        return millis == null ? "—" : Instant.ofEpochMilli(millis).toString();
    }
}
