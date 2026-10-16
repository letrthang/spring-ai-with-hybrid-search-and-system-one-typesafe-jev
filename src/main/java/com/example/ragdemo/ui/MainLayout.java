package com.example.ragdemo.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

/** The one page: a header, and a left drawer with the three menus. Every view is shown inside it. */
@Layout
public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 title = new H1("RAG demo");
        title.addClassNames("text-lg", "m-0");
        Span subtitle = new Span("System 1 + System 2 + Weaviate hybrid search");
        subtitle.addClassNames("text-sm", "rd-muted", "hidden", "sm:inline");
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, subtitle);
        header.addClassNames("items-center", "gap-3", "px-2", "w-full");
        addToNavbar(header);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("💬 Chat", ChatView.class));
        nav.addItem(new SideNavItem("🔎 Search Data", SearchDataView.class));
        nav.addItem(new SideNavItem("🔑 Key Management", KeyManagementView.class));
        nav.addClassNames("p-2");
        addToDrawer(nav);
    }
}
