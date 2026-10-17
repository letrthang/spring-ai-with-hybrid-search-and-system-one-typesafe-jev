package com.example.ragdemo.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;

/**
 * The one page: a header and a left drawer with the three menus. Every view is shown inside it.
 *
 * <p>The header title sits in the centre; along the header's bottom border two slow wave lines drift like an ocean
 * swell. Pure CSS (no script, no image file), and everything stands still for people who ask for reduced motion.
 * On the left, after ☰: the version this build runs (the release version on the live site), linked to its release
 * notes — in the drawer instead below 1100 px. On the right: the Spring AI, Vaadin, Hugging Face (Laya model) and GitHub
 * logos and the language dropdown (English · Tiếng Việt · 日本語).
 */
@Layout
public class MainLayout extends AppLayout {

    private static final String GITHUB = AppVersion.GITHUB;
    private static final String LAYA_MODEL = "https://huggingface.co/letrthang/aimtas-laya-multilingual-GGUF";

    public MainLayout() {
        H1 title = new H1(Tr.t("header.title"));
        title.addClassNames("rd-brand", "m-0");
        Span subtitle = new Span(Tr.t("header.subtitle"));
        subtitle.addClassNames("rd-brand-sub", "hidden", "sm:inline");
        Div text = new Div(title, subtitle);
        text.addClassNames("rd-brand-text", "flex", "flex-col", "items-center");

        Div center = new Div(text);
        center.addClassNames("rd-brand-center", "flex", "items-center", "justify-center");

        DrawerToggle toggle = new DrawerToggle();
        Div right = new Div(builtWith(), languages());
        right.addClassNames("rd-built-with", "flex", "items-center", "gap-2", "ml-auto");
        Div header = new Div(toggle, version("rd-version"), center, right);
        header.addClassNames("rd-header", "flex", "items-center", "w-full", "px-2", "gap-2");
        addToNavbar(header);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem(Tr.t("menu.chat"), ChatView.class));
        nav.addItem(new SideNavItem(Tr.t("menu.search"), SearchDataView.class));
        nav.addItem(new SideNavItem(Tr.t("menu.keys"), KeyManagementView.class));
        nav.addClassNames("p-2");
        Div drawer = new Div(nav, version("rd-version-drawer"));
        drawer.addClassNames("flex", "flex-col", "h-full");
        addToDrawer(drawer);
    }

    /** "Version v1.1.0" (in the page's language), linked to its release notes; nothing when the build has no version. */
    private static Anchor version(String className) {
        Anchor link = new Anchor(AppVersion.notesUrl(),
                AppVersion.VERSION == null ? "" : Tr.t("header.versionLabel", AppVersion.VERSION));
        link.setTarget("_blank");
        link.getElement().setAttribute("rel", "noopener");
        link.getElement().setAttribute("title", AppVersion.released()
                ? Tr.t("header.version", AppVersion.VERSION) : Tr.t("header.versionDev", AppVersion.VERSION));
        link.addClassName(className);
        link.setVisible(AppVersion.VERSION != null);
        return link;
    }

    /**
     * 🇬🇧 English · 🇻🇳 Tiếng Việt · 🇯🇵 日本語. The choice is kept for the session; the page reloads so every view is
     * built again in the new language.
     */
    private static Select<Locale> languages() {
        Select<Locale> select = new Select<>();
        select.setItems(Tr.LANGUAGES);
        select.setItemLabelGenerator(l -> switch (l.getLanguage()) {
            case "vi" -> "🇻🇳 Tiếng Việt";
            case "ja" -> "🇯🇵 日本語";
            default -> "🇬🇧 English";
        });
        select.setValue(Tr.LANGUAGES.contains(Tr.locale()) ? Tr.locale() : Tr.ENGLISH);
        select.addClassName("rd-language");
        select.setAriaLabel(Tr.t("header.language"));
        select.addValueChangeListener(e -> {
            if (e.getValue() == null || e.getValue().equals(e.getOldValue()) || !e.isFromClient()) return;
            VaadinSession.getCurrent().setLocale(e.getValue());
            UI.getCurrent().setLocale(e.getValue());
            UI.getCurrent().getPage().reload();
        });
        return select;
    }

    /**
     * "Built with" Spring AI and Vaadin logos, then the Laya model on Hugging Face and this repository on GitHub
     * (WebP; Spring AI logo from spring.io, Vaadin and GitHub from simple-icons, Hugging Face from huggingface.co).
     */
    private static Div builtWith() {
        Anchor github = logo("icons/github.webp", "GitHub", GITHUB, Tr.t("header.sourceCode"));
        github.getChildren().findFirst().ifPresent(image -> image.addClassName("rd-logo-mono"));
        Div logos = new Div(
                logo("icons/spring-ai.webp", "Spring AI", "https://spring.io/projects/spring-ai",
                        Tr.t("header.builtWith", "Spring AI")),
                logo("icons/vaadin.webp", "Vaadin", "https://vaadin.com", Tr.t("header.builtWith", "Vaadin")),
                logo("icons/huggingface.webp", "Hugging Face", LAYA_MODEL, Tr.t("header.layaModel")),
                github);
        logos.addClassNames("flex", "items-center", "gap-3");
        return logos;
    }

    private static Anchor logo(String src, String name, String href, String tooltip) {
        Image image = new Image(src, name + " logo");
        image.addClassName("rd-logo");
        Span label = new Span(name);
        label.addClassName("rd-logo-label");
        Anchor link = new Anchor(href, image, label);
        link.setTarget("_blank");
        link.getElement().setAttribute("rel", "noopener");
        link.getElement().setAttribute("title", tooltip);
        link.addClassNames("flex", "items-center", "gap-1");
        return link;
    }
}
