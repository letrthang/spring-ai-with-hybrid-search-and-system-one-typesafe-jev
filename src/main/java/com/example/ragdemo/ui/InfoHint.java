package com.example.ragdemo.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/**
 * The ℹ️ button next to an important part of the UI. A click opens a small dialog that explains it like a lesson:
 * <b>what it is · what it does here · linked to · a little theory · learn more</b>. Texts live in {@link Hint}.
 */
public class InfoHint extends Button {

    public InfoHint(Hint hint) {
        this(hint, null);
    }

    /** "ℹ️ BM25" — a hint with a short label, for rows of terms. */
    public InfoHint(Hint hint, String label) {
        super(label == null ? "ℹ️" : "ℹ️ " + label);
        addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        addClassName("rd-info");
        getElement().setAttribute("aria-label", "What is this? " + hint.title);
        getElement().setAttribute("title", "What is this?");
        addClickListener(e -> dialog(hint).open());
    }

    static Dialog dialog(Hint hint) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(hint.title);
        Button close = new Button("✕", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.TERTIARY);
        close.getElement().setAttribute("aria-label", "Close");
        dialog.getHeader().add(close);

        Div body = new Div(
                section("What it is", hint.what),
                section("What it does here", hint.does));
        if (!hint.links.isEmpty()) {
            Div chips = new Div();
            chips.addClassNames("flex", "flex-wrap", "gap-1");
            hint.links.forEach(l -> {
                Span chip = new Span(l);
                chip.addClassNames("rd-status", "rd-status-UNKNOWN", "text-xs");
                chips.add(chip);
            });
            body.add(label("Linked to"), chips);
        }
        Div theory = new Div(label("📘 A little theory"), text(hint.theory));
        theory.addClassNames("rd-hint-theory", "p-3", "flex", "flex-col", "gap-1");
        body.add(theory);
        if (hint.learnMore != null) {
            Anchor more = new Anchor(hint.learnMore, "🔗 Learn more");
            more.setTarget("_blank");
            more.getElement().setAttribute("rel", "noopener");
            more.addClassName("text-sm");
            body.add(more);
        }
        body.addClassNames("flex", "flex-col", "gap-2");
        dialog.add(body);
        dialog.setWidth("min(480px, 95vw)");
        dialog.addClassName("rd-hint-dialog");
        return dialog;
    }

    private static Div section(String title, String content) {
        Div d = new Div(label(title), text(content));
        d.addClassNames("flex", "flex-col", "gap-1");
        return d;
    }

    private static H4 label(String text) {
        H4 h = new H4(text);
        h.addClassNames("m-0", "text-xs", "rd-hint-label");
        return h;
    }

    private static Paragraph text(String content) {
        Paragraph p = new Paragraph(content);
        p.addClassNames("m-0", "text-sm");
        return p;
    }

    /**
     * A chip of generated data (a score, a rank, a verdict) that is itself a hint: "BM25 #1 ℹ️". Clicking it opens
     * the lesson for that kind of value, so every number on screen can explain itself.
     */
    public static Button chip(String text, Hint hint, String... extraClasses) {
        Button chip = new Button(text + " ℹ️", e -> dialog(hint).open());
        chip.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        chip.addClassNames("rd-status", "rd-status-UNKNOWN", "rd-info-chip");
        chip.addClassNames(extraClasses);
        chip.getElement().setAttribute("title", "What does this mean?");
        chip.getElement().setAttribute("aria-label", text + " — what does this mean? " + hint.title);
        return chip;
    }

    /** The hint for a 🔬 Trace line or a status, by its leading emoji. */
    public static Hint forStep(String step) {
        if (step.startsWith("🗄️")) return Hint.WEAVIATE;
        if (step.startsWith("⚡")) return Hint.SYSTEM1;
        if (step.startsWith("🧠")) return Hint.SYSTEM2;
        if (step.startsWith("⛔")) return Hint.FAIL_CLOSED;
        return Hint.TRACE;
    }

    /** A heading or label followed by its ℹ️ button, kept on one line. */
    public static Div with(com.vaadin.flow.component.Component labelled, Hint hint) {
        Div row = new Div(labelled, new InfoHint(hint));
        row.addClassNames("flex", "items-center", "gap-1", "min-w-0");
        return row;
    }
}
