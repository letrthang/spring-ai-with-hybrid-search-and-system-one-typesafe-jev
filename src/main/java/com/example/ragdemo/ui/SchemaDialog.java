package com.example.ragdemo.ui;

import com.example.ragdemo.weaviate.DemoDocCollection;
import com.example.ragdemo.weaviate.WeaviateSchema;
import com.example.ragdemo.weaviate.WeaviateSchema.Field;
import com.example.ragdemo.weaviate.WeaviateSchema.Schema;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;

/**
 * 🧱 Schema of the {@value DemoDocCollection#NAME} collection (🔎 Search Data → 🧱 Schema), read live from Weaviate
 * with {@code GET /v1/schema/DemoDoc}: the collection, its vectorizer and vector index, every property with its type,
 * tokenization and indexes, and the raw JSON exactly as Weaviate answered.
 */
public class SchemaDialog extends AppDialog {

    public SchemaDialog(WeaviateSchema schemas) {
        super(Tr.t("schema.title", DemoDocCollection.NAME));
        setFixedHeight("min(85vh, 820px)");   // the schema is read in the background: never resize when it lands
        Span loading = new Span(Tr.t("schema.reading"));
        loading.addClassNames("rd-muted", "text-sm");
        Div body = new Div(loading);
        body.addClassNames("flex", "flex-col", "gap-4");
        add(body);
        body.addAttachListener(e -> {
            if (!e.isInitialAttach()) return;
            Background.run(body, schemas::fetch, schema -> {
                body.remove(loading);
                if (schema == null) {
                    body.add(new Span(Tr.t("schema.none")));
                } else {
                    body.add(source(schema), summary(schema), properties(schema), raw(schema));
                }
            }, error -> loading.setText("⚠️ " + Tr.t(error instanceof com.example.ragdemo.rag.SystemDownException down
                    ? down.reason() : com.example.ragdemo.rag.Msg.raw(Background.message(error)))));
        });
    }

    /** "📡 GET /v1/schema/DemoDoc" and the full address it was read from. */
    private static Div source(Schema s) {
        Span call = new Span("📡 GET /v1/schema/" + DemoDocCollection.NAME);
        call.addClassName("rd-wv-mono");
        Span url = new Span(Tr.t("schema.source") + " " + s.url());
        url.addClassNames("rd-muted", "text-xs", "break-all");
        Div box = new Div(call, url);
        box.addClassNames("flex", "flex-col", "gap-1");
        return box;
    }

    private static Div summary(Schema s) {
        String vectorizer = s.vectorizer() + (s.vectorSources().isEmpty() ? ""
                : " · " + Tr.t("schema.vectorFrom") + " " + String.join(" + ", s.vectorSources()));
        Div rows = new Div(
                RecordDialog.field("class", s.collection()),
                RecordDialog.field("description", s.description().isBlank() ? "—" : s.description()),
                RecordDialog.field("vectorizer", vectorizer),
                RecordDialog.field("vectorIndexType", s.vectorIndex()));
        rows.addClassNames("flex", "flex-col", "gap-2");
        Div card = new Div(rows);
        card.addClassName("rd-wv");
        return section(Tr.t("schema.collection"), Hint.SCHEMA, card);
    }

    private static Div properties(Schema s) {
        Div table = new Div();
        table.addClassName("rd-schema-table");
        table.add(cell(Tr.t("schema.col.name"), "rd-schema-head"), cell(Tr.t("schema.col.type"), "rd-schema-head"),
                cell(Tr.t("schema.col.tokenization"), "rd-schema-head"), cell(Tr.t("schema.col.searchable"), "rd-schema-head"),
                cell(Tr.t("schema.col.filterable"), "rd-schema-head"));
        for (Field f : s.fields()) {
            Span name = new Span(f.name());
            name.addClassName("rd-wv-key");
            Div nameCell = new Div(name);
            nameCell.addClassName("rd-schema-cell");
            table.add(nameCell, cell(f.dataType(), "rd-wv-mono"), cell(f.tokenization(), "rd-wv-mono"),
                    cell(f.searchable() ? "✅" : "—", null), cell(f.filterable() ? "✅" : "—", null));
        }
        Span note = new Span(Tr.t("schema.note"));
        note.addClassNames("rd-muted", "text-xs");
        Div box = new Div(table, note);
        box.addClassNames("flex", "flex-col", "gap-2");
        return section(Tr.t("schema.properties", s.fields().size()), Hint.SCHEMA, box);
    }

    private static Details raw(Schema s) {
        Pre pre = new Pre(s.json());
        pre.addClassNames("rd-wv-json", "m-0");
        Details details = new Details(Tr.t("schema.raw"), pre);
        details.setOpened(false);
        return details;
    }

    private static Div cell(String text, String className) {
        Div cell = new Div(new Span(text));
        cell.addClassName("rd-schema-cell");
        if (className != null) cell.addClassName(className);
        return cell;
    }

    private static Div section(String title, Hint hint, Div content) {
        H3 heading = new H3(title);
        heading.addClassNames("m-0", "text-sm");
        Div section = new Div(InfoHint.with(heading, hint), content);
        section.addClassNames("rd-record-section", "flex", "flex-col", "gap-2", "p-3");
        return section;
    }
}
