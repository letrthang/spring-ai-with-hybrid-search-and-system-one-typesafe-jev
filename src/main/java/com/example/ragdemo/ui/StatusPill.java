package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.vaadin.flow.component.html.Span;

/** A 🟢 / 🟡 / 🔴 pill for one service, e.g. "🟢 ⚡ System 1: Laya is ready". */
public class StatusPill extends Span {

    private final ServiceStatus.Service service;

    public StatusPill(ServiceStatus.Service service) {
        this.service = service;
        addClassName("rd-status");
        showChecking();
    }

    public void showChecking() {
        setState("UNKNOWN", "⏳ " + service.label + ": checking…");
    }

    public void show(ServiceStatus status) {
        setState(status.state().name(), status.text());
    }

    private void setState(String state, String text) {
        getElement().getClassList().removeIf(c -> c.startsWith("rd-status-"));
        addClassName("rd-status-" + state);
        setText(text);
    }
}
