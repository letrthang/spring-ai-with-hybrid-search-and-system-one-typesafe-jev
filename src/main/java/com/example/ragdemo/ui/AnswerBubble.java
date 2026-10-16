package com.example.ragdemo.ui;

import com.example.ragdemo.rag.RagAnswer;
import com.example.ragdemo.system1.SystemOneFilter;
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
            Paragraph failed = new Paragraph(Tr.t("answer.failed", "@" + a.failure().service().key(),
                    Tr.t(a.failure().reason())));
            failed.addClassNames("m-0", "rd-warning", "p-2");
            bubble.add(InfoHint.with(failed, Hint.FAIL_CLOSED));
        } else {
            // nothing kept → a translated explanation; otherwise the LLM's answer (in the language of the question)
            Markdown text = new Markdown(a.nothingKept() ? Tr.t("answer.nothingKept") : a.text());
            text.addClassName("rd-markdown");
            bubble.add(text);
            if (a.nothingKept()) bubble.add(InfoHint.chip(Tr.t("answer.whyNoAnswer"), Hint.LAYA_SCORES));
            Button agent = agentChip(a);
            if (agent != null) bubble.add(agent);
            Button learning = learningChip(a);
            if (learning != null) bubble.add(learning);
        }
        Div flowRow = new Div(dataFlowButton(a), new InfoHint(Hint.DATA_FLOW));
        flowRow.addClassNames("flex", "items-center", "gap-1");
        bubble.add(sources(a), trace(a), flowRow);
        return bubble;
    }

    // ------------------------------------------------------------------ 🧠 self-learning

    /** 🛠 Agent · rounds · tool calls, with the calls in its tooltip — null outside agentic mode. */
    static Button agentChip(RagAnswer a) {
        if (!a.agentic()) return null;
        Button chip = InfoHint.chip(Tr.t("agent.chip", a.agent().rounds(), a.agent().toolCalls()), Hint.AGENTIC,
                "rd-learn-chip", "rd-agent-chip");
        chip.getElement().setAttribute("title", Tr.t("agent.calls") + "\n" + String.join("\n", a.agent().calls()));
        return chip;
    }

    /** ♻️ reused a learned answer · 🧠 learned ✅ · 🧠 not learned (why) — null when learning did not apply. */
    static Button learningChip(RagAnswer a) {
        if (a.reused()) {
            RagAnswer.Reuse r = a.reuse();
            Button chip = InfoHint.chip(Tr.t("learn.reusedChip", String.format(java.util.Locale.ROOT, "%.3f", r.distance()),
                    String.format(java.util.Locale.ROOT, "%.2f", r.answers())), Hint.LEARNING, "rd-learn-chip", "rd-learn-reused");
            chip.getElement().setAttribute("title", Tr.t("learn.reusedFrom", r.learnedQuestion()));
            return chip;
        }
        RagAnswer.Learning l = a.learning();
        if (l == null) return null;
        if (l.saved()) {
            return InfoHint.chip(Tr.t("learn.savedChip", two(l.answers()), two(l.grounded()), two(l.injection())),
                    Hint.LEARNING, "rd-learn-chip", "rd-learn-saved");
        }
        return InfoHint.chip(Tr.t("learn.notSavedChip", Tr.t(l.reason())), Hint.LEARNING, "rd-learn-chip");
    }

    private static String two(Double d) {
        return d == null ? "—" : String.format(java.util.Locale.ROOT, "%.2f", d);
    }

    // ------------------------------------------------------------------ 📚 Sources

    private static Details sources(RagAnswer a) {
        Div list = new Div();
        list.addClassNames("flex", "flex-col", "gap-2");
        Div learn = new Div(muted(Tr.t("answer.learn")), new InfoHint(Hint.SOURCES, Tr.t("answer.learn.sources")),
                new InfoHint(Hint.BM25, "BM25"), new InfoHint(Hint.VECTOR, Tr.t("answer.learn.vector")),
                new InfoHint(Hint.RRF, "RRF"), new InfoHint(Hint.LAYA_SCORES, Tr.t("answer.learn.scores")),
                new InfoHint(Hint.INJECTION, Tr.t("answer.learn.injection")));
        learn.addClassNames("flex", "flex-wrap", "items-center", "gap-1");
        list.add(learn);
        if (a.sources().isEmpty()) list.add(muted(Tr.t("answer.noPassage")));
        for (Source s : a.sources()) list.add(source(s));
        Details details = new Details(Tr.t("answer.sources", a.usedCount(), a.sources().size()), list);
        details.addClassName("rd-answer-section");
        return details;
    }

    private static Div source(Source s) {
        Span title = new Span((s.used() ? "[" + s.citation() + "] " : "") + s.title());
        title.addClassNames("font-semibold", "text-sm");
        Span from = new Span("📄 " + s.source() + (s.chunkNo() == null ? "" : " · " + Tr.t("common.passage", s.chunkNo())));
        from.addClassNames("rd-muted", "text-xs");
        Div chips = new Div(
                InfoHint.chip("BM25 " + (s.bm25Rank() == null ? "—" : "#" + s.bm25Rank()), Hint.BM25),
                InfoHint.chip(Tr.t("common.vector") + " " + (s.vectorRank() == null ? "—" : "#" + s.vectorRank()), Hint.VECTOR),
                InfoHint.chip("RRF " + (s.rrfScore() == null ? "—" : String.format(Locale.ROOT, "%.4f", s.rrfScore())),
                        Hint.RRF));
        if (s.relevant() != null) {
            chips.add(InfoHint.chip("⚡ " + Tr.t("flow.scores", fmt(s.relevant()), fmt(s.answers()), fmt(s.injection())),
                    Hint.LAYA_SCORES));
        }
        if (s.verdict() != null) {
            chips.add(InfoHint.chip((s.verdict().equals("kept") ? "✅ " : "🚫 ") + Tr.t(SystemOneFilter.verdictKey(s.verdict()))
                            + (s.verdict().equals("kept") && !s.used() ? " " + Tr.t("answer.notInBest") : ""),
                    s.verdict().contains("injection") ? Hint.INJECTION : Hint.LAYA_SCORES,
                    s.verdict().equals("kept") ? "rd-status-READY" : "rd-status-DOWN"));
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
            Span step = new Span(Tr.t(line.step()));
            step.addClassNames("font-semibold");
            Div stepCell = InfoHint.with(step, InfoHint.forStep(line.step()));
            stepCell.addClassNames("shrink-0", "w-36");
            Span detail = new Span(Tr.t(line.detail()));
            detail.addClassNames("min-w-0", "break-words");
            Div row = new Div(stepCell, detail);
            row.addClassNames("flex", "gap-2");
            lines.add(row);
        }
        lines.add(new InfoHint(Hint.TRACE, Tr.t("answer.traceHint")));
        Details details = new Details(Tr.t("answer.trace"), lines);
        details.addClassName("rd-answer-section");
        return details;
    }

    // ------------------------------------------------------------------ 🔀 Data flow

    private static Button dataFlowButton(RagAnswer a) {
        Button open = new Button(Tr.t("answer.dataFlow"), e -> new AppDialog(Tr.t("answer.dataFlowTitle", shortQuestion(a.question())),
                DataFlowDiagram.of(a.flow())).open());
        open.addThemeVariants(ButtonVariant.TERTIARY);
        open.addClassName("self-start");
        return open;
    }

    private static String shortQuestion(String q) {
        return q.length() <= 60 ? q : q.substring(0, 59) + "…";
    }


    private static String fmt(Double v) {
        return v == null ? "—" : String.format(Locale.ROOT, "%.2f", v);
    }

    private static Span muted(String text) {
        Span span = new Span(text);
        span.addClassNames("rd-muted", "text-sm");
        return span;
    }
}
