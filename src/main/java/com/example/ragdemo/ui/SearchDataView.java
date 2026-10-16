package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.SystemDownException;
import com.example.ragdemo.weaviate.HybridRetriever;
import com.example.ragdemo.weaviate.HybridRetriever.Hit;
import com.example.ragdemo.weaviate.HybridRetriever.Mode;
import com.example.ragdemo.weaviate.SampleDataLoader;
import com.example.ragdemo.weaviate.WeaviateHealth;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Locale;

/**
 * 🔎 Search Data: look inside Weaviate. The same question in the three modes shows <i>why</i> hybrid search exists:
 * BM25 and vector search rank the passages differently, and RRF merges both.
 *
 * <p>An empty query lists the passages (the demo has a few dozen, so the first {@value #BROWSE_LIMIT} are loaded at
 * once). Every Weaviate call runs on a virtual thread.
 */
@Route("search")
@PageTitle("Search Data · RAG demo")
public class SearchDataView extends Div {

    static final int BROWSE_LIMIT = 200;
    static final int SEARCH_LIMIT = 40;   // 20 BM25 + 20 vector at most, fewer after merging

    private final HybridRetriever retriever;
    private final SampleDataLoader loader;
    private final WeaviateHealth health;

    private final TextField query = new TextField();
    private final RadioButtonGroup<Mode> mode = new RadioButtonGroup<>();
    private final Button searchButton = new Button("🔎 Search");
    private final Button loadButton = new Button("📥 Load sample data");
    private final Button clearButton = new Button("🗑 Clear collection");
    private final StatusPill pill = new StatusPill(ServiceStatus.Service.WEAVIATE);
    private final Paragraph warning = new Paragraph();
    private final Span resultInfo = new Span();
    private final Grid<Hit> grid = new Grid<>();
    private final MasterDetailLayout masterDetail = new MasterDetailLayout();
    private final Div gridCard = new Div();
    private List<Hit> items = List.of();
    private ServiceStatus status;
    private boolean busy;

    public SearchDataView(HybridRetriever retriever, SampleDataLoader loader, WeaviateHealth health) {
        this.retriever = retriever;
        this.loader = loader;
        this.health = health;
        addClassNames("p-4", "flex", "flex-col", "gap-3", "h-full", "box-border");

        Paragraph intro = new Paragraph("Search the Weaviate collection by keywords (BM25), by meaning (vector) or "
                + "both merged with RRF (hybrid). Try \"reset AER-01\" and \"fish breathing at the top\" in each mode.");
        intro.addClassNames("rd-muted", "m-0");
        warning.addClassNames("rd-warning", "m-0", "p-3");
        warning.setVisible(false);

        add(new H2("🔎 Search Data"), intro, toolbar(), statusRow(), warning, results());
        addAttachListener(e -> checkHealth());   // not in the constructor: background work needs the attached UI
    }

    // ------------------------------------------------------------------ layout

    private Div toolbar() {
        query.setPlaceholder("Question or keywords — empty = list all");
        query.setClearButtonVisible(true);
        query.addClassNames("grow", "min-w-0");
        query.addKeyPressListener(Key.ENTER, e -> search());

        mode.setItems(Mode.values());
        mode.setItemLabelGenerator(m -> switch (m) {
            case BM25 -> "BM25";
            case VECTOR -> "Vector";
            case HYBRID -> "Hybrid";
        });
        mode.setValue(Mode.HYBRID);
        mode.addThemeVariants(RadioGroupVariant.AURA_HORIZONTAL);
        mode.addValueChangeListener(e -> {
            if (!query.isEmpty()) search();
        });

        searchButton.addThemeVariants(ButtonVariant.PRIMARY);
        searchButton.addClickListener(e -> search());
        loadButton.addClickListener(e -> loadSampleData());
        clearButton.addThemeVariants(ButtonVariant.TERTIARY);
        clearButton.addClickListener(e -> confirmClear());

        Div searchRow = new Div(query, searchButton);
        searchRow.addClassNames("flex", "gap-2", "items-center", "w-full", "lg:w-auto", "lg:grow");
        Div actions = new Div(mode, loadButton, clearButton);
        actions.addClassNames("flex", "flex-wrap", "gap-2", "items-center");
        Div bar = new Div(searchRow, actions);
        bar.addClassNames("rd-card", "p-3", "flex", "flex-wrap", "gap-3", "items-center");
        return bar;
    }

    private Div statusRow() {
        Span hint = new Span("Click selects · click again to close · double-click a row for 🔍 Details…");
        hint.addClassNames("rd-muted", "text-sm");
        Div row = new Div(pill, resultInfo, hint);
        row.addClassNames("flex", "flex-wrap", "gap-3", "items-center");
        resultInfo.addClassNames("text-sm");
        return row;
    }

    private Div results() {
        grid.addColumn(h -> items.indexOf(h) + 1).setHeader("#").setWidth("4em").setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Hit::title).setHeader("Title").setFlexGrow(4);
        grid.addColumn(Hit::source).setHeader("Source").setFlexGrow(2);
        grid.addColumn(h -> rank(h.bm25Rank(), h.bm25Score(), "%.2f")).setHeader("BM25 rank · score").setFlexGrow(1);
        grid.addColumn(h -> rank(h.vectorRank(), h.vectorDistance(), "%.3f")).setHeader("Vector rank · distance")
                .setFlexGrow(1);
        grid.addColumn(h -> h.rrfScore() == null ? "—" : String.format(Locale.ROOT, "%.4f", h.rrfScore()))
                .setHeader("RRF score").setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        grid.getColumns().forEach(c -> c.setResizable(true));
        grid.addThemeVariants(GridVariant.ROW_STRIPES);
        grid.setEmptyStateText("No passages. Load the sample data, or try another query.");
        grid.setSizeFull();

        // single click selects → detail card; clicking the selected row again deselects → card closes
        grid.asSingleSelect().addValueChangeListener(e -> masterDetail.setDetail(
                e.getValue() == null ? null : detailCard(e.getValue())));
        grid.addItemDoubleClickListener(e -> {
            grid.select(e.getItem());
            new AppDialog("🔍 " + e.getItem().title(), detailCard(e.getItem())).open();
        });

        masterDetail.setMaster(grid);
        masterDetail.setMasterSize("min(560px, 100%)");
        masterDetail.setDetailSize("380px");
        masterDetail.addBackdropClickListener(e -> grid.deselectAll());
        masterDetail.addDetailEscapePressListener(e -> grid.deselectAll());
        masterDetail.setSizeFull();

        Button fullScreen = new Button("⛶", e -> toggleFullScreen());
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.getElement().setAttribute("title", "Full screen (Esc to exit)");
        fullScreen.getElement().setAttribute("aria-label", "Full screen");
        Span title = new Span("📄 Passages");
        title.addClassNames("font-semibold");
        Div head = new Div(title, fullScreen);
        head.addClassNames("flex", "justify-between", "items-center");

        gridCard.add(head, masterDetail);
        gridCard.addClassNames("rd-card", "p-3", "flex", "flex-col", "gap-2", "grow", "min-h-[420px]");
        Shortcuts.addShortcutListener(gridCard, () -> {
            if (gridCard.hasClassName("rd-fullscreen")) toggleFullScreen();
        }, Key.ESCAPE).listenOn(gridCard);
        return gridCard;
    }

    /** The detail card: the passage, where it comes from, and how each search ranked it. */
    static Div detailCard(Hit hit) {
        H3 title = new H3(hit.title());
        title.addClassNames("m-0", "text-base");
        Span source = new Span("📄 " + hit.source() + (hit.chunkNo() == null ? "" : " · passage " + hit.chunkNo()));
        source.addClassNames("rd-muted", "text-sm");
        Div chips = new Div(
                chip("BM25 " + rank(hit.bm25Rank(), hit.bm25Score(), "%.2f")),
                chip("Vector " + rank(hit.vectorRank(), hit.vectorDistance(), "%.3f")),
                chip("RRF " + (hit.rrfScore() == null ? "—" : String.format(Locale.ROOT, "%.4f", hit.rrfScore()))));
        chips.addClassNames("flex", "flex-wrap", "gap-2");
        Paragraph body = new Paragraph(hit.body());
        body.addClassNames("m-0", "whitespace-pre-line");
        Div card = new Div(title, source, chips, body);
        card.addClassNames("p-4", "flex", "flex-col", "gap-3");
        return card;
    }

    private static Span chip(String text) {
        Span chip = new Span(text);
        chip.addClassNames("rd-status", "rd-status-UNKNOWN");
        return chip;
    }

    /** "#2 · 3.41", or "—" when the passage was not in that list. */
    static String rank(Integer rank, Float value, String format) {
        if (rank == null) return "—";
        return "#" + rank + (value == null ? "" : " · " + String.format(Locale.ROOT, format, value));
    }

    private void toggleFullScreen() {
        if (gridCard.hasClassName("rd-fullscreen")) {
            gridCard.removeClassName("rd-fullscreen");
        } else {
            gridCard.addClassName("rd-fullscreen");
        }
    }

    // ------------------------------------------------------------------ actions (all on virtual threads)

    private void checkHealth() {
        pill.showChecking();
        Background.run(this, health::check, this::showHealth);
    }

    private void showHealth(ServiceStatus newStatus) {
        status = newStatus;
        pill.show(status);
        warning.setVisible(!status.isReady());
        warning.setText("⚠️ " + status.service().label + ": " + status.reason());
        applyEnabled();
        if (status.isReady() && items.isEmpty()) search();
    }

    /** Search needs a ready collection; Load / Clear need only a reachable Weaviate. Nothing while busy. */
    private void applyEnabled() {
        boolean ready = status != null && status.isReady();
        boolean reachable = status != null && status.state() != ServiceStatus.State.DOWN;
        query.setEnabled(ready);
        searchButton.setEnabled(ready && !busy);
        loadButton.setEnabled(reachable && !busy);
        clearButton.setEnabled(reachable && !busy);
    }

    private void search() {
        String text = query.getValue().strip();
        Mode m = mode.getValue();
        setBusy(true, text.isEmpty() ? "⏳ loading…" : "⏳ searching…");
        Background.run(this, () -> {
            if (text.isEmpty()) {
                List<Hit> all = retriever.list(0, BROWSE_LIMIT);
                return new Shown(all, all.size() + " passages (no query)");
            }
            HybridRetriever.Result r = retriever.search(text, m, SEARCH_LIMIT);
            return new Shown(r.hits(), r.hits().size() + " passages · " + describe(r, m));
        }, shown -> {
            items = shown.hits;
            grid.setItems(items);
            resultInfo.setText(shown.info);
            setBusy(false, null);
        }, this::failed);
    }

    private static String describe(HybridRetriever.Result r, Mode m) {
        String bm25 = "BM25 " + r.bm25Count() + " in " + r.bm25Ms() + " ms";
        String vector = "vector " + r.vectorCount() + " in " + r.vectorMs() + " ms";
        return switch (m) {
            case BM25 -> bm25;
            case VECTOR -> vector;
            case HYBRID -> bm25 + " · " + vector + " · merged by RRF";
        };
    }

    private void loadSampleData() {
        setBusy(true, "⏳ loading sample data into Weaviate…");
        Background.run(this, loader::load, count -> {
            Notification.show("📥 " + count + " passages loaded").addThemeVariants(NotificationVariant.SUCCESS);
            setBusy(false, null);
            items = List.of();
            checkHealth();
        }, this::failed);
    }

    private void confirmClear() {
        Paragraph text = new Paragraph("Delete the whole DemoDoc collection from Weaviate? You can load the sample "
                + "data again afterwards.");
        AppDialog dialog = new AppDialog("🗑 Clear collection", text);
        Button delete = new Button("🗑 Delete collection", e -> {
            setBusy(true, "⏳ deleting…");
            Background.run(this, () -> {
                loader.deleteCollection();
                return true;
            }, done -> {
                items = List.of();
                grid.setItems(items);
                setBusy(false, null);
                checkHealth();
            }, this::failed);
        });
        delete.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.PRIMARY);
        delete.addClickListener(e -> dialog.close());
        dialog.getFooter().add(delete);
        dialog.setWidth("min(480px, 100vw)");
        dialog.open();
    }

    private void failed(RuntimeException e) {
        setBusy(false, null);
        if (e instanceof SystemDownException down) {
            showHealth(down.toStatus());
        } else {
            Notification.show("⚠️ " + e.getMessage()).addThemeVariants(NotificationVariant.ERROR);
        }
    }

    private void setBusy(boolean isBusy, String text) {
        busy = isBusy;
        applyEnabled();
        if (text != null) resultInfo.setText(text);
    }

    private record Shown(List<Hit> hits, String info) {
    }
}
