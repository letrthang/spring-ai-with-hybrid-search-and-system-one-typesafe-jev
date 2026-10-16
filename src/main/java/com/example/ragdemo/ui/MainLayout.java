package com.example.ragdemo.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

/**
 * The one page: a header and a left drawer with the three menus. Every view is shown inside it.
 *
 * <p>The header title sits in the centre over a slowly moving, softly glowing colour band (in the spirit of Siri's
 * animation): pure CSS, no script, and it stands still for people who ask for reduced motion.
 */
@Layout
public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 title = new H1("RAG demo");
        title.addClassNames("rd-brand", "m-0");
        Span subtitle = new Span("⚡ System 1 · 🧠 System 2 · 🗄️ Weaviate hybrid search");
        subtitle.addClassNames("rd-brand-sub", "hidden", "sm:inline");
        Div text = new Div(title, subtitle);
        text.addClassNames("rd-brand-text", "flex", "flex-col", "items-center");

        Div glow = new Div();
        glow.addClassName("rd-siri-glow");
        glow.getElement().setAttribute("aria-hidden", "true");
        Div center = new Div(glow, text);
        center.addClassNames("rd-brand-center", "flex", "items-center", "justify-center");

        DrawerToggle toggle = new DrawerToggle();
        Div spacer = new Div();   // same width as the toggle, so the title is truly centred
        spacer.addClassName("rd-toggle-spacer");
        Div header = new Div(toggle, center, spacer);
        header.addClassNames("rd-header", "flex", "items-center", "w-full", "px-2", "gap-2");
        addToNavbar(header);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("💬 Chat", ChatView.class));
        nav.addItem(new SideNavItem("🔎 Search Data", SearchDataView.class));
        nav.addItem(new SideNavItem("🔑 Key Management", KeyManagementView.class));
        nav.addClassNames("p-2");
        addToDrawer(nav);
    }
}
