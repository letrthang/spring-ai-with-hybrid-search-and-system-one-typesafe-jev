package com.example.ragdemo.ui;

import com.example.ragdemo.ui.RecordDialog.Row;
import com.example.ragdemo.weaviate.LearnedAnswers;
import com.example.ragdemo.weaviate.LearnedAnswers.Learned;
import com.example.ragdemo.weaviate.LearnedAnswers.LearnedSource;
import com.example.ragdemo.weaviate.WeaviateRecords;
import com.example.ragdemo.weaviate.WeaviateRecords.StoredRecord;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;

import java.util.List;
import java.util.Locale;

/**
 * 🔍 Details of one learned answer (🧠 Learned answers: double-click or right click → Details…): the question and the
 * answer with the ⚡ System 1 scores that let it be saved, the passages it came from, then — read live from Weaviate —
 * the stored object as the Weaviate console shows it and where it lives, like {@link RecordDialog} for a passage.
 */
public class LearnedDialog extends AppDialog {

    public LearnedDialog(Learned l, WeaviateRecords records) {
        super("🧠 " + l.question());
        setFixedHeight("min(85vh, 860px)");   // the object is read in the background: never resize when it lands
        Span loading = new Span(Tr.t("record.reading"));
        loading.addClassNames("rd-muted", "text-sm");
        Div body = new Div(answer(l), sources(l.sources()), loading);
        body.addClassNames("flex", "flex-col", "gap-4");
        add(body);
        // on attach, not on "opened": background work needs the content attached to the UI
        body.addAttachListener(e -> {
            if (!e.isInitialAttach()) return;
            Background.run(body, () -> records.fetch(l.id()), stored -> {
                body.remove(loading);
                if (stored == null) {
                    body.add(new Span(Tr.t("record.gone")));
                } else {
                    body.add(RecordDialog.object(stored), where(stored, l));
                }
            }, error -> loading.setText("⚠️ " + error.getMessage()));
        });
    }

    /** The answer as the chat showed it, and why it was learned. */
    private static Div answer(Learned l) {
        Markdown text = new Markdown(l.answer());
        text.addClassName("rd-markdown");
        Div chips = new Div(
                InfoHint.chip(Tr.t("learned.dialog.scores", two(l.answersScore()), two(l.groundedScore()),
                        two(l.injectionScore())), Hint.LEARNING, "rd-learn-saved"),
                InfoHint.chip(Tr.t("learned.dialog.lang", l.lang()), Hint.SCHEMA),
                InfoHint.chip("🧠 " + (l.model().isBlank() ? "—" : l.model()), Hint.MODELS));
        chips.addClassNames("flex", "flex-wrap", "gap-2");
        Div box = new Div(text, chips);
        box.addClassNames("flex", "flex-col", "gap-2");
        return RecordDialog.section(Tr.t("learned.dialog.answer"), Hint.LEARNING, box);
    }

    /** "[1] title · file · section n" and the passage text — what the answer cites. */
    private static Div sources(List<LearnedSource> sources) {
        Div list = new Div();
        list.addClassNames("flex", "flex-col", "gap-3");
        if (sources.isEmpty()) list.add(new Span(Tr.t("learned.dialog.noSources")));
        for (LearnedSource s : sources) {
            Span title = new Span((s.citation() == null ? "" : "[" + s.citation() + "] ") + s.title());
            title.addClassName("font-semibold");
            Span where = new Span("📄 " + s.source() + (s.chunkNo() == null ? "" : " · " + Tr.t("common.passage", s.chunkNo())));
            where.addClassNames("rd-muted", "text-xs");
            Paragraph text = new Paragraph(s.text());
            text.addClassNames("m-0", "text-sm", "whitespace-pre-line");
            Div item = new Div(title, where, text);
            item.addClassNames("flex", "flex-col", "gap-1");
            list.add(item);
        }
        return RecordDialog.section(Tr.t("learned.dialog.sources", sources.size()), Hint.SOURCES, list);
    }

    private static Div where(StoredRecord r, Learned l) {
        String key = l.lang() + "|" + LearnedAnswers.normalized(l.question());
        Div rows = RecordDialog.table(List.of(
                new Row(Tr.t("record.cluster"), r.cluster(), Hint.WEAVIATE),
                new Row(Tr.t("record.collection"), r.collection() + " · kind = learned", Hint.LEARNING),
                new Row(Tr.t("record.uuid"), r.uuid(), Hint.RECORD),
                new Row(Tr.t("record.uuidHow"), Tr.t("learned.dialog.uuidHow", key), Hint.RECORD),
                new Row(Tr.t("record.times"), RecordDialog.time(r.createdAt()) + " / " + RecordDialog.time(r.updatedAt()),
                        Hint.RECORD),
                new Row(Tr.t("record.rest"), r.cluster() + "/v1/objects/" + r.collection() + "/" + r.uuid(), Hint.RECORD)));
        return RecordDialog.section(Tr.t("record.where"), Hint.RECORD, rows);
    }

    private static String two(Double d) {
        return d == null ? "—" : String.format(Locale.ROOT, "%.2f", d);
    }
}
