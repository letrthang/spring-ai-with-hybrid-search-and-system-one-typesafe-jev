package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.vaadin.flow.component.html.Span;

/** A 🟢 / 🟡 / 🔴 pill for one service, e.g. "🟢 ⚡ System 1: Laya is ready" (in the page's language). */
public class StatusPill extends Span {

    private final ServiceStatus.Service service;

    public StatusPill(ServiceStatus.Service service) {
        this.service = service;
        addClassName("rd-status");
        showChecking();
    }

    public void showChecking() {
        setState("UNKNOWN", "⏳ " + Tr.t(service.key()) + ": " + Tr.t("status.checking"));
    }

    public void show(ServiceStatus status) {
        setState(status.state().name(), text(status));
    }

    /** "🟢 ⚡ System 1: Laya is ready" — the dot, the service and the translated reason. */
    public static String text(ServiceStatus status) {
        return status.state().dot + " " + Tr.t(status.service().key()) + ": " + Tr.t(status.reason());
    }

    private void setState(String state, String text) {
        getElement().getClassList().removeIf(c -> c.startsWith("rd-status-"));
        addClassName("rd-status-" + state);
        setText(text);
    }
}
