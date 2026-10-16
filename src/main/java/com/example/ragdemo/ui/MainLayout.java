package com.example.ragdemo.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

/**
 * The one page: a header and a left drawer with the three menus. Every view is shown inside it.
 *
 * <p>The header title sits in the centre; along the header's bottom border two slow wave lines drift like an ocean
 * swell. Pure CSS (no script, no image file), and everything stands still for people who ask for reduced motion.
 */
@Layout
public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 title = new H1("RAG demo with System 1 & 2 and Hybrid Search");
        title.addClassNames("rd-brand", "m-0");
        Span subtitle = new Span("Spring AI · Weaviate · Laya · OpenRouter");
        subtitle.addClassNames("rd-brand-sub", "hidden", "sm:inline");
        Div text = new Div(title, subtitle);
        text.addClassNames("rd-brand-text", "flex", "flex-col", "items-center");

        Div center = new Div(text);
        center.addClassNames("rd-brand-center", "flex", "items-center", "justify-center");

        DrawerToggle toggle = new DrawerToggle();
        Div header = new Div(toggle, center, builtWith());
        header.addClassNames("rd-header", "flex", "items-center", "w-full", "px-2", "gap-2");
        addToNavbar(header);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("💬 Chat", ChatView.class));
        nav.addItem(new SideNavItem("🔎 Search Data", SearchDataView.class));
        nav.addItem(new SideNavItem("🔑 Key Management", KeyManagementView.class));
        nav.addClassNames("p-2");
        addToDrawer(nav);
    }

    /** "Built with" Spring AI and Vaadin logos on the right (WebP; Spring AI logo from spring.io, Vaadin from simple-icons). */
    private static Div builtWith() {
        Div logos = new Div(logo("icons/spring-ai.webp", "Spring AI", "https://spring.io/projects/spring-ai"),
                logo("icons/vaadin.webp", "Vaadin", "https://vaadin.com"));
        logos.addClassNames("rd-built-with", "flex", "items-center", "gap-3", "ml-auto");
        return logos;
    }

    private static Anchor logo(String src, String name, String href) {
        Image image = new Image(src, name + " logo");
        image.addClassName("rd-logo");
        Span label = new Span(name);
        label.addClassNames("rd-logo-label", "hidden", "md:inline");
        Anchor link = new Anchor(href, image, label);
        link.setTarget("_blank");
        link.getElement().setAttribute("rel", "noopener");
        link.getElement().setAttribute("title", "Built with " + name);
        link.addClassNames("flex", "items-center", "gap-1");
        return link;
    }
}
