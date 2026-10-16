package com.example.ragdemo.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder — built in its own sub-issue. */
@Route("keys")
@PageTitle("Key Management · RAG demo")
public class KeyManagementView extends Div {

    public KeyManagementView() {
        addClassNames("p-4", "flex", "flex-col", "gap-2");
        add(new H2("🔑 Key Management"), new Paragraph("Enter the Weaviate and OpenRouter keys."));
    }
}
