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
    private String normalHeight;

    public AppDialog(String title, Component... content) {
        setHeaderTitle(title);
        Button fullScreen = new Button("⛶");
        fullScreen.getElement().setAttribute("aria-label", Tr.t("common.fullScreen"));
        fullScreen.getElement().setAttribute("title", Tr.t("common.fullScreen"));
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.addClickListener(e -> toggleFullScreen());
        Button close = new Button("✕", e -> close());
        close.getElement().setAttribute("aria-label", Tr.t("common.close"));
        close.getElement().setAttribute("title", Tr.t("common.close"));
        close.addThemeVariants(ButtonVariant.TERTIARY);
        getHeader().add(fullScreen, close);

        add(content);
        setWidth("min(900px, 100vw)");
        setMaxHeight("90vh");
        setResizable(true);
        setDraggable(true);
    }

    /**
     * A fixed height for content that arrives later (read in the background): the dialog opens at its final size
     * and the content scrolls inside, instead of the dialog growing and jumping when the data lands.
     */
    public void setFixedHeight(String height) {
        normalHeight = height;
        if (!full) setHeight(height);
    }

    private void toggleFullScreen() {
        full = !full;
        setWidth(full ? "100vw" : "min(900px, 100vw)");
        setHeight(full ? "100vh" : normalHeight);
        setMaxHeight(full ? "100vh" : "90vh");
    }
}
