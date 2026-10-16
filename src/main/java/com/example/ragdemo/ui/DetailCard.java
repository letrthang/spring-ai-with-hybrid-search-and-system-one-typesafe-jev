package com.example.ragdemo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;

/**
 * The card shown on the right of a master-detail grid: a framed card with its title and a ✕ close button at the
 * top right. ✕ calls {@code onClose} (the grid deselects its row, which closes the card).
 */
public class DetailCard extends Div {

    public DetailCard(String title, Runnable onClose, Component... body) {
        H3 heading = new H3(title);
        heading.addClassNames("m-0", "text-base", "min-w-0", "break-words");
        Button close = new Button("✕", e -> onClose.run());
        close.addThemeVariants(ButtonVariant.TERTIARY);
        close.addClassName("shrink-0");
        close.getElement().setAttribute("aria-label", "Close");
        close.getElement().setAttribute("title", "Close");
        Div header = new Div(heading, close);
        header.addClassNames("rd-detail-header", "flex", "items-start", "justify-between", "gap-2", "px-4", "pt-3",
                "pb-2");

        Div content = new Div(body);
        content.addClassNames("flex", "flex-col", "gap-3", "px-4", "pb-4", "pt-3", "min-w-0");

        add(header, content);
        addClassNames("rd-detail", "flex", "flex-col", "min-w-0");
    }
}
