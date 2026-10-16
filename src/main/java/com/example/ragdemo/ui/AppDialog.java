package com.example.ragdemo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * The house dialog: a title, and ⛶ full screen + ✕ close at the top right (never a text "Close" button).
 * Esc also closes it. Long content scrolls inside the dialog, not the page.
 */
public class AppDialog extends Dialog {

    private boolean full;

    public AppDialog(String title, Component... content) {
        setHeaderTitle(title);
        Button fullScreen = new Button("⛶");
        fullScreen.getElement().setAttribute("aria-label", "Full screen");
        fullScreen.getElement().setAttribute("title", "Full screen");
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.addClickListener(e -> toggleFullScreen());
        Button close = new Button("✕", e -> close());
        close.getElement().setAttribute("aria-label", "Close");
        close.getElement().setAttribute("title", "Close");
        close.addThemeVariants(ButtonVariant.TERTIARY);
        getHeader().add(fullScreen, close);

        add(content);
        setWidth("min(900px, 100vw)");
        setMaxHeight("90vh");
        setResizable(true);
        setDraggable(true);
    }

    private void toggleFullScreen() {
        full = !full;
        setWidth(full ? "100vw" : "min(900px, 100vw)");
        setHeight(full ? "100vh" : null);
        setMaxHeight(full ? "100vh" : "90vh");
    }
}
