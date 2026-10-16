package com.example.ragdemo.ui;

import com.example.ragdemo.weaviate.LearnedAnswers;
import com.example.ragdemo.weaviate.LearnedAnswers.Learned;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;

import java.util.Locale;

/**
 * 🧠 Learned answers (🔎 Search Data): what the app learned — every answer here was verified by ⚡ System 1 before
 * it was saved (DemoDoc objects with kind = learned). Lazy grid (one Weaviate page per scroll), a count, and 🗑 Forget all.
 */
public class LearnedCard extends Div {

    private final LearnedAnswers learned;
    private final Grid<Learned> grid = new Grid<>();
    private final Span count = new Span();

    public LearnedCard(LearnedAnswers learned) {
        this.learned = learned;
        addClassNames("rd-card", "p-4", "flex", "flex-col", "gap-3");

        H3 title = new H3(Tr.t("learned.title"));
        title.addClassNames("m-0", "text-base");
        Span lead = new Span(Tr.t("learned.lead"));
        lead.addClassNames("rd-muted", "text-sm");
        count.addClassNames("rd-status", "rd-status-UNKNOWN", "text-xs");
        Button refresh = new Button(Tr.t("db.refresh"), e -> load());
        refresh.addThemeVariants(ButtonVariant.TERTIARY);
        Button forget = new Button(Tr.t("learned.forget"), e -> confirmForget());
        forget.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.ERROR);
        // ⛶ full screen (Esc or ⛶ again to leave), like the 💾 App database card
        Button fullScreen = new Button("⛶", e -> toggleFullScreen());
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.addClassName("ml-auto");
        fullScreen.getElement().setAttribute("title", Tr.t("common.fullScreenEsc"));
        fullScreen.getElement().setAttribute("aria-label", Tr.t("common.fullScreen"));
        Div bar = new Div(InfoHint.with(title, Hint.LEARNING), count, refresh, forget, fullScreen);
        bar.addClassNames("flex", "flex-wrap", "items-center", "gap-3");
        Shortcuts.addShortcutListener(this, () -> {
            if (hasClassName("rd-fullscreen")) toggleFullScreen();
        }, Key.ESCAPE).listenOn(this);

        grid.addColumn(Learned::question).setHeader(Tr.t("learned.col.question")).setFlexGrow(3).setResizable(true);
        grid.addColumn(l -> shorten(l.answer())).setHeader(Tr.t("learned.col.answer")).setFlexGrow(4).setResizable(true);
        grid.addColumn(Learned::lang).setHeader(Tr.t("learned.col.lang")).setWidth("5em").setFlexGrow(0);
        grid.addColumn(l -> two(l.answersScore()) + " · " + two(l.groundedScore()))
                .setHeader(Tr.t("learned.col.scores")).setWidth("10em").setFlexGrow(0);
        grid.addColumn(Learned::model).setHeader("model").setFlexGrow(2).setResizable(true);
        grid.addThemeVariants(GridVariant.ROW_STRIPES);
        grid.setEmptyStateText(Tr.t("learned.empty"));
        grid.setHeight("280px");
        grid.addClassName("rd-learned-grid");

        add(bar, lead, grid);
        addAttachListener(e -> {
            if (e.isInitialAttach()) load();
        });
    }

    /** The count first (background), then the grid pages from Weaviate as it scrolls. */
    private void load() {
        Background.run(this, () -> {
            try {
                return learned.count();
            } catch (RuntimeException e) {
                return 0L;   // Weaviate not ready: show an empty list, the status pill explains why
            }
        }, n -> {
            count.setText(Tr.t("learned.count", n));
            grid.setItems(q -> learned.list(q.getOffset(), q.getLimit()).stream(), q -> n.intValue());
        });
    }

    private void toggleFullScreen() {
        if (hasClassName("rd-fullscreen")) removeClassName("rd-fullscreen");
        else addClassName("rd-fullscreen");
    }

    private void confirmForget() {
        Paragraph text = new Paragraph(Tr.t("learned.forgetConfirm"));
        text.addClassName("m-0");
        AppDialog dialog = new AppDialog(Tr.t("learned.forget"), text);
        dialog.setWidth("min(480px, 100vw)");
        Button yes = new Button(Tr.t("learned.forget"), e -> Background.run(this, () -> {
            learned.forgetAll();
            return true;
        }, done -> {
            dialog.close();
            Notification.show(Tr.t("learned.count", 0));
            load();
        }));
        yes.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.PRIMARY);
        dialog.getFooter().add(yes);
        dialog.open();
    }

    private static String shorten(String s) {
        return s == null || s.length() <= 140 ? s : s.substring(0, 140) + "…";
    }

    private static String two(Double d) {
        return d == null ? "—" : String.format(Locale.ROOT, "%.2f", d);
    }
}
