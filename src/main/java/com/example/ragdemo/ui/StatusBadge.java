package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.vaadin.flow.component.html.Span;

/**
 * A compact "● online" / "● starting" / "● offline" badge for the top-right corner of a card; the full sentence
 * (e.g. "🟢 ⚡ System 1: Laya is ready") is its tooltip. Same states as {@link StatusPill}.
 */
public class StatusBadge extends Span {

    public StatusBadge() {
        addClassName("rd-badge");
        showChecking();
    }

    public void showChecking() {
        set("UNKNOWN", Tr.t("map.checking"), null);
    }

    public void show(ServiceStatus status) {
        String text = switch (status.state()) {
            case READY -> Tr.t("map.online");
            case WAITING -> Tr.t("map.waiting");
            case DOWN -> Tr.t("map.offline");
        };
        set(status.state().name(), text, StatusPill.text(status));
    }

    private void set(String state, String text, String tooltip) {
        getElement().getClassList().removeIf(c -> c.startsWith("rd-badge-"));
        addClassName("rd-badge-" + state);
        removeAll();
        Span dot = new Span();
        dot.addClassName("rd-badge-dot");
        add(dot, new Span(text));
        if (tooltip == null) getElement().removeAttribute("title");
        else getElement().setAttribute("title", tooltip);
        getElement().setAttribute("aria-label", tooltip == null ? text : tooltip);
    }
}
