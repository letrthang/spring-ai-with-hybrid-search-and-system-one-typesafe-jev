package com.example.ragdemo.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder — built in its own sub-issue. */
@Route("search")
@PageTitle("Search Data · RAG demo")
public class SearchDataView extends Div {

    public SearchDataView() {
        addClassNames("p-4", "flex", "flex-col", "gap-2");
        add(new H2("🔎 Search Data"), new Paragraph("Look inside Weaviate with BM25, vector or hybrid search."));
    }
}
