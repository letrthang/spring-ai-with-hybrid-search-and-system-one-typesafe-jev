package com.example.ragdemo.ui;

import com.example.ragdemo.rag.RagAnswer;
import com.example.ragdemo.rag.RagAnswer.Source;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;

import java.util.Locale;

/**
 * The chat bubbles: the user's question, and the AI answer with its three sections —
 * 📚 Sources (what was found and how each step judged it), 🔬 Trace (timings, model, tokens) and
 * 🔀 Data flow (a dialog with the sequence diagram).
 */
public final class AnswerBubble {

    private AnswerBubble() {
    }

    public static Div question(String text) {
        Paragraph p = new Paragraph(text);
        p.addClassNames("m-0", "whitespace-pre-line");
        Div bubble = new Div(p);
        bubble.addClassNames("rd-bubble", "rd-bubble-user", "p-3");
        return bubble;
    }

    public static Div answer(RagAnswer a) {
        Div bubble = new Div();
        bubble.addClassNames("rd-bubble", "rd-bubble-ai", "p-3", "flex", "flex-col", "gap-2");
        if (a.failed()) {
            Paragraph failed = new Paragraph("⚠️ " + a.failure().service().label + " is not available: "
                    + a.failure().reason() + ". The question was not answered.");
            failed.addClassNames("m-0", "rd-warning", "p-2");
            bubble.add(failed);
        } else {
            Markdown text = new Markdown(a.text());
            text.addClassName("rd-markdown");
            bubble.add(text);
        }
        Div flowRow = new Div(dataFlowButton(a), new InfoHint(Hint.DATA_FLOW));
        flowRow.addClassNames("flex", "items-center", "gap-1");
        bubble.add(sources(a), trace(a), flowRow);
        return bubble;
    }

    // ------------------------------------------------------------------ 📚 Sources

    private static Details sources(RagAnswer a) {
        Div list = new Div();
        list.addClassNames("flex", "flex-col", "gap-2");
        Div learn = new Div(muted("Learn:"), new InfoHint(Hint.SOURCES, "Sources"), new InfoHint(Hint.BM25, "BM25"),
                new InfoHint(Hint.VECTOR, "Vector"), new InfoHint(Hint.RRF, "RRF"),
                new InfoHint(Hint.LAYA_SCORES, "relevant · answers · injection"),
                new InfoHint(Hint.INJECTION, "Prompt injection"));
        learn.addClassNames("flex", "flex-wrap", "items-center", "gap-1");
        list.add(learn);
        if (a.sources().isEmpty()) list.add(muted("No passage was found."));
        for (Source s : a.sources()) list.add(source(s));
        Details details = new Details("📚 Sources — " + a.usedCount() + " used of " + a.sources().size(), list);
        details.addClassName("rd-answer-section");
        return details;
    }

    private static Div source(Source s) {
        Span title = new Span((s.used() ? "[" + s.citation() + "] " : "") + s.title());
        title.addClassNames("font-semibold", "text-sm");
        Span from = new Span("📄 " + s.source() + (s.chunkNo() == null ? "" : " · passage " + s.chunkNo()));
        from.addClassNames("rd-muted", "text-xs");
        Div chips = new Div(
                chip("BM25 " + (s.bm25Rank() == null ? "—" : "#" + s.bm25Rank())),
                chip("Vector " + (s.vectorRank() == null ? "—" : "#" + s.vectorRank())),
                chip("RRF " + (s.rrfScore() == null ? "—" : String.format(Locale.ROOT, "%.4f", s.rrfScore()))));
        if (s.relevant() != null) {
            chips.add(chip(String.format(Locale.ROOT, "⚡ relevant %.2f · answers %.2f · injection %.2f",
                    s.relevant(), s.answers(), s.injection())));
        }
        if (s.verdict() != null) {
            Span verdict = chip((s.verdict().equals("kept") ? "✅ " : "🚫 ") + s.verdict()
                    + (s.verdict().equals("kept") && !s.used() ? " (not in the best 3)" : ""));
            verdict.addClassName(s.verdict().equals("kept") ? "rd-status-READY" : "rd-status-DOWN");
            chips.add(verdict);
        }
        chips.addClassNames("flex", "flex-wrap", "gap-1");
        Paragraph text = new Paragraph(s.text());
        text.addClassNames("m-0", "text-sm", "rd-muted", "line-clamp-3");
        Div card = new Div(title, from, chips, text);
        card.addClassNames("rd-source", "p-2", "flex", "flex-col", "gap-1");
        if (!s.used()) card.addClassName("rd-source-unused");
        return card;
    }

    // ------------------------------------------------------------------ 🔬 Trace

    private static Details trace(RagAnswer a) {
        Div lines = new Div();
        lines.addClassNames("flex", "flex-col", "gap-1", "text-sm");
        for (RagAnswer.TraceLine line : a.trace()) {
            Span step = new Span(line.step());
            step.addClassNames("font-semibold", "shrink-0", "w-32");
            Span detail = new Span(line.detail());
            detail.addClassNames("min-w-0", "break-words");
            Div row = new Div(step, detail);
            row.addClassNames("flex", "gap-2");
            lines.add(row);
        }
        lines.add(new InfoHint(Hint.TRACE, "What do these numbers mean?"));
        Details details = new Details("🔬 Trace", lines);
        details.addClassName("rd-answer-section");
        return details;
    }

    // ------------------------------------------------------------------ 🔀 Data flow

    private static Button dataFlowButton(RagAnswer a) {
        Button open = new Button("🔀 Data flow", e -> new AppDialog("🔀 Data flow — " + shortQuestion(a.question()),
                DataFlowDiagram.of(a.flow())).open());
        open.addThemeVariants(ButtonVariant.TERTIARY);
        open.addClassName("self-start");
        return open;
    }

    private static String shortQuestion(String q) {
        return q.length() <= 60 ? q : q.substring(0, 59) + "…";
    }

    private static Span chip(String text) {
        Span chip = new Span(text);
        chip.addClassNames("rd-status", "rd-status-UNKNOWN", "text-xs");
        return chip;
    }

    private static Span muted(String text) {
        Span span = new Span(text);
        span.addClassNames("rd-muted", "text-sm");
        return span;
    }
}
