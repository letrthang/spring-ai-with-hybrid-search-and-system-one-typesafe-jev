package com.example.ragdemo.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder — built in its own sub-issue. */
@Route("")
@PageTitle("Chat · RAG demo")
public class ChatView extends Div {

    public ChatView() {
        addClassNames("p-4", "flex", "flex-col", "gap-2");
        add(new H2("💬 Chat"), new Paragraph("Ask a question; the answer shows its sources, trace and data flow."));
    }
}
