package com.example.ragdemo.ui;

import com.example.ragdemo.rag.FlowRecorder.Actor;
import com.example.ragdemo.rag.FlowRecorder.FlowHop;
import com.example.ragdemo.rag.FlowRecorder.Kind;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeDetails;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

import java.util.List;
import java.util.Locale;

/**
 * 🔀 Data flow: one question as a <b>sequence diagram</b>, drawn in plain HTML/CSS (no image, no library), so it
 * follows the light/dark theme and its text can be selected.
 *
 * <p>One column (lifeline) per actor, one numbered row per hop, top to bottom in time order. The words go above the
 * arrow, "+start · duration" under it. Click a row to see its details. On a narrow screen the diagram keeps its width
 * and scrolls sideways inside the dialog, never the page.
 */
public final class DataFlowDiagram {

    private static final List<Actor> ACTORS = List.of(Actor.values());

    private DataFlowDiagram() {
    }

    public static Div of(List<FlowHop> hops) {
        int n = ACTORS.size();
        Div heads = new Div();
        heads.addClassName("rd-flow-heads");
        for (Actor actor : ACTORS) {
            Span name = new Span(actor.label);
            Div head = InfoHint.with(name, switch (actor) {
                case USER, APP -> Hint.RAG;
                case WEAVIATE -> Hint.WEAVIATE;
                case SYSTEM1 -> Hint.SYSTEM1;
                case SYSTEM2 -> Hint.SYSTEM2;
            });
            head.addClassNames("rd-flow-actor", "justify-center");
            heads.add(head);
        }

        Div body = new Div();
        body.addClassName("rd-flow-body");
        for (int i = 0; i < n; i++) {
            Div life = new Div();
            life.addClassName("rd-flow-life");
            life.getElement().getStyle().set("left", pct((i + 0.5) / n));
            body.add(life);
        }
        for (FlowHop hop : hops) body.add(row(hop, n));

        Div canvas = new Div(heads, body);
        canvas.addClassName("rd-flow-canvas");
        Div legendRow = new Div(legend(hops), new InfoHint(Hint.DATA_FLOW));
        legendRow.addClassNames("flex", "items-center", "gap-1");
        Div diagram = new Div(canvas, legendRow);
        diagram.addClassName("rd-flow");
        return diagram;
    }

    /** One hop: the arrow (or a note on one lifeline) and, when opened, its summary. */
    private static NativeDetails row(FlowHop hop, int n) {
        int from = ACTORS.indexOf(hop.from());
        int to = ACTORS.indexOf(hop.to());
        boolean self = from == to;
        int lo = Math.min(from, to);
        double left = (lo + 0.5) / n;
        double width = self ? 0.35 / n : (double) Math.abs(to - from) / n;

        Div arrow = new Div();
        arrow.addClassNames("rd-flow-arrow", "rd-flow-" + hop.kind().name().toLowerCase(Locale.ROOT));
        if (to < from) arrow.addClassName("rd-flow-rev");
        if (self) arrow.addClassName("rd-flow-self");
        place(arrow, left, width);

        // the text is centred over the arrow, at least two columns wide so short arrows stay readable
        double textWidth = Math.min(1.0, Math.max(width, 2.0 / n));
        double textLeft = Math.max(0, Math.min(1 - textWidth, left + width / 2 - textWidth / 2));
        Span number = new Span(String.valueOf(hop.seq()));
        number.addClassName("rd-flow-n");
        Span text = new Span(hop.label());
        text.addClassName("rd-flow-text");
        Div label = new Div(number, text);
        label.addClassName("rd-flow-label");
        place(label, textLeft, textWidth);

        Span timing = new Span("+" + hop.atMs() + " ms" + (hop.ms() > 0 ? " · " + hop.ms() + " ms" : ""));
        Div meta = new Div(timing);
        meta.addClassName("rd-flow-meta");
        place(meta, textLeft, textWidth);

        Div line = new Div(arrow, label, meta);
        line.addClassName("rd-flow-row");
        Paragraph summary = new Paragraph(hop.summary().isBlank() ? "(nothing more to show)" : hop.summary());
        summary.addClassNames("rd-flow-summary", "m-0");

        NativeDetails step = new NativeDetails(line, summary);
        step.addClassName("rd-flow-step");
        step.getElement().setAttribute("title", hop.from().label + " → " + hop.to().label + ": " + hop.label());
        return step;
    }

    private static Span legend(List<FlowHop> hops) {
        long calls = hops.stream().filter(h -> h.kind() == Kind.CALL).count();
        long last = hops.stream().mapToLong(h -> h.atMs() + h.ms()).max().orElse(0);
        Span legend = new Span(hops.size() + " steps · " + calls + " calls · " + last + " ms in total — "
                + "solid arrow = call, green = reply, grey loop = a step inside the app. Click a row for details.");
        legend.addClassNames("rd-muted", "text-xs");
        return legend;
    }

    private static void place(Div element, double left, double width) {
        element.getElement().getStyle().set("left", pct(left)).set("width", pct(width));
    }

    private static String pct(double fraction) {
        return String.format(Locale.ROOT, "%.3f%%", fraction * 100);
    }
}
