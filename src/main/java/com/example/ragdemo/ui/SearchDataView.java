package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.SystemDownException;
import com.example.ragdemo.weaviate.HybridRetriever;
import com.example.ragdemo.weaviate.HybridRetriever.Hit;
import com.example.ragdemo.weaviate.HybridRetriever.Mode;
import com.example.ragdemo.weaviate.SampleDataLoader;
import com.example.ragdemo.weaviate.WeaviateHealth;
import com.example.ragdemo.weaviate.WeaviateRecords;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
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
public class SearchDataView extends Div implements HasDynamicTitle {

    static final int BROWSE_LIMIT = 200;
    static final int SEARCH_LIMIT = 40;   // 20 BM25 + 20 vector at most, fewer after merging

    private final HybridRetriever retriever;
    private final SampleDataLoader loader;
    private final WeaviateHealth health;
    private final WeaviateRecords records;
    private Grid.Column<Hit> bm25Column;
    private Grid.Column<Hit> vectorColumn;
    private Grid.Column<Hit> rrfColumn;

    private final TextField query = new TextField();
    private final RadioButtonGroup<Mode> mode = new RadioButtonGroup<>();
    private final Button searchButton = new Button(Tr.t("search.search"));
    private final Button loadButton = new Button(Tr.t("search.load"));
    private final Button clearButton = new Button(Tr.t("search.clear"));
    private final StatusPill pill = new StatusPill(ServiceStatus.Service.WEAVIATE);
    private final Paragraph warning = new Paragraph();
    private final Span resultInfo = new Span();
    private final Grid<Hit> grid = new Grid<>();
    private final MasterDetailLayout masterDetail = new MasterDetailLayout();
    private final Div gridCard = new Div();
    private List<Hit> items = List.of();
    private ServiceStatus status;
    private boolean busy;

    public SearchDataView(HybridRetriever retriever, SampleDataLoader loader, WeaviateHealth health,
                          WeaviateRecords records) {
        this.retriever = retriever;
        this.loader = loader;
        this.health = health;
        this.records = records;
        addClassNames("p-4", "flex", "flex-col", "gap-3", "h-full", "box-border");

        Paragraph intro = new Paragraph(Tr.t("search.intro"));
        intro.addClassNames("rd-muted", "m-0");
        warning.addClassNames("rd-warning", "m-0", "p-3");
        warning.setVisible(false);

        add(InfoHint.with(new H2(Tr.t("search.title")), Hint.WEAVIATE), intro, toolbar(), statusRow(), warning, results());
        addAttachListener(e -> checkHealth());   // not in the constructor: background work needs the attached UI
    }

    // ------------------------------------------------------------------ layout

    private Div toolbar() {
        query.setPlaceholder(Tr.t("search.placeholder"));
        query.setClearButtonVisible(true);
        query.addClassNames("grow", "min-w-0");
        query.addKeyPressListener(Key.ENTER, e -> search());

        mode.setItems(Mode.values());
        mode.setItemLabelGenerator(m -> switch (m) {
            case BM25 -> "BM25";
            case VECTOR -> Tr.t("common.vector");
            case HYBRID -> Tr.t("search.hybrid");
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
        Div actions = new Div(mode, new InfoHint(Hint.SEARCH_MODES), loadButton, new InfoHint(Hint.CHUNKS),
                clearButton);
        actions.addClassNames("flex", "flex-wrap", "gap-2", "items-center");
        Div bar = new Div(searchRow, actions);
        bar.addClassNames("rd-card", "p-3", "flex", "flex-wrap", "gap-3", "items-center");
        return bar;
    }

    private Div statusRow() {
        Span hint = new Span(Tr.t("grid.clickHint"));
        hint.addClassNames("rd-muted", "text-sm");
        Div row = new Div(pill, InfoHint.with(resultInfo, Hint.SEARCH_MODES), hint);
        row.addClassNames("flex", "flex-wrap", "gap-3", "items-center");
        resultInfo.addClassNames("text-sm");
        return row;
    }

    private Div results() {
        grid.addColumn(h -> items.indexOf(h) + 1).setHeader(InfoHint.with(new Span("#"), Hint.ROW_ORDER)).setWidth("4em").setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Hit::title).setHeader(InfoHint.with(new Span(Tr.t("search.col.title")), Hint.CHUNKS)).setFlexGrow(4);
        grid.addColumn(Hit::source).setHeader(InfoHint.with(new Span(Tr.t("search.col.source")), Hint.RECORD)).setFlexGrow(2);
        // every score cell is a clickable lesson chip: "#1 · 9.81 ℹ️"
        bm25Column = grid.addComponentColumn(h -> InfoHint.chip(rank(h.bm25Rank(), h.bm25Score(), "%.2f"), Hint.BM25))
                .setHeader(InfoHint.with(new Span(Tr.t("search.col.bm25")), Hint.BM25))
                .setFlexGrow(1);
        vectorColumn = grid.addComponentColumn(h -> InfoHint.chip(rank(h.vectorRank(), h.vectorDistance(), "%.3f"),
                        Hint.VECTOR))
                .setHeader(InfoHint.with(new Span(Tr.t("search.col.vector")), Hint.VECTOR)).setFlexGrow(1);
        rrfColumn = grid.addComponentColumn(h -> InfoHint.chip(
                        h.rrfScore() == null ? "—" : String.format(Locale.ROOT, "%.4f", h.rrfScore()), Hint.RRF))
                .setHeader(InfoHint.with(new Span(Tr.t("search.col.rrf")), Hint.RRF)).setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        showScoreColumns(null);
        grid.getColumns().forEach(c -> c.setResizable(true));
        grid.addThemeVariants(GridVariant.ROW_STRIPES);
        grid.setEmptyStateText(Tr.t("search.empty"));
        grid.setSizeFull();

        // single click selects → detail card; clicking the selected row again deselects → card closes
        grid.asSingleSelect().addValueChangeListener(e -> masterDetail.setDetail(
                e.getValue() == null ? null : new DetailCard("📄 " + e.getValue().title(), grid::deselectAll,
                        detailBody(e.getValue()))));
        grid.addItemDoubleClickListener(e -> {
            grid.select(e.getItem());
            new RecordDialog(e.getItem(), records, detailBody(e.getItem())).open();
        });

        masterDetail.setMaster(grid);
        masterDetail.setMasterSize("min(560px, 100%)");
        masterDetail.setDetailSize("380px");
        masterDetail.addBackdropClickListener(e -> grid.deselectAll());
        masterDetail.addDetailEscapePressListener(e -> grid.deselectAll());
        masterDetail.setSizeFull();

        Button fullScreen = new Button("⛶", e -> toggleFullScreen());
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.getElement().setAttribute("title", Tr.t("common.fullScreenEsc"));
        fullScreen.getElement().setAttribute("aria-label", Tr.t("common.fullScreen"));
        Div title = InfoHint.with(new Span(Tr.t("search.passages")), Hint.RECORD);
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

    /** What the detail card shows: where the passage comes from, how each search ranked it, and its text. */
    static Div detailBody(Hit hit) {
        Span source = new Span("📄 " + hit.source() + (hit.chunkNo() == null ? "" : " · " + Tr.t("common.passage", hit.chunkNo())));
        source.addClassNames("rd-muted", "text-sm");
        Div chips = new Div(
                InfoHint.chip("BM25 " + rank(hit.bm25Rank(), hit.bm25Score(), "%.2f"), Hint.BM25),
                InfoHint.chip(Tr.t("common.vector") + " " + rank(hit.vectorRank(), hit.vectorDistance(), "%.3f"), Hint.VECTOR),
                InfoHint.chip("RRF " + (hit.rrfScore() == null ? "—" : String.format(Locale.ROOT, "%.4f",
                        hit.rrfScore())), Hint.RRF));
        chips.addClassNames("flex", "flex-wrap", "gap-2");
        Paragraph body = new Paragraph(hit.body());
        body.addClassNames("m-0", "whitespace-pre-line");
        boolean searched = hit.bm25Rank() != null || hit.vectorRank() != null;
        Paragraph meaning = new Paragraph(searched
                ? Tr.t("search.detail.searched")
                : Tr.t("search.detail.list"));
        meaning.addClassNames("rd-muted", "text-xs", "m-0");
        Span textLabel = new Span(Tr.t("search.detail.text"));
        textLabel.addClassNames("rd-muted", "text-xs");
        Div content = new Div(source, chips, meaning, textLabel, body);
        content.addClassNames("flex", "flex-col", "gap-3");
        return content;
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
        Background.run(this, health::check, this::showHealth,
                e -> showHealth(ServiceStatus.down(ServiceStatus.Service.WEAVIATE, "status.checkFailed", Background.message(e))));
    }

    private void showHealth(ServiceStatus newStatus) {
        status = newStatus;
        pill.show(status);
        warning.setVisible(!status.isReady());
        warning.setText("⚠️ " + Tr.t(status.service().key()) + ": " + Tr.t(status.reason()));
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
        setBusy(true, Tr.t(text.isEmpty() ? "search.loading" : "search.searching"));
        Background.run(this, () -> {
            if (text.isEmpty()) {
                return new Shown(retriever.list(0, BROWSE_LIMIT), null);
            }
            HybridRetriever.Result r = retriever.search(text, m, SEARCH_LIMIT);
            return new Shown(r.hits(), r);
        }, shown -> {
            // texts are built here, on the page (in its language), not on the background thread
            items = shown.hits;
            grid.setItems(items);
            showScoreColumns(text.isEmpty() ? null : m);
            resultInfo.setText(shown.result == null ? Tr.t("search.info.list", items.size())
                    : Tr.t("search.info.search", items.size(), describe(shown.result, m)));
            setBusy(false, null);
        }, this::failed);
    }

    /**
     * Only the columns the mode fills: BM25 → BM25 rank; Vector → vector rank; Hybrid → both + RRF.
     * Listing all passages (no query) has no ranks at all.
     */
    private void showScoreColumns(Mode m) {
        bm25Column.setVisible(m == Mode.BM25 || m == Mode.HYBRID);
        vectorColumn.setVisible(m == Mode.VECTOR || m == Mode.HYBRID);
        rrfColumn.setVisible(m == Mode.HYBRID);
    }

    private static String describe(HybridRetriever.Result r, Mode m) {
        String bm25 = Tr.t("search.info.bm25", r.bm25Count(), r.bm25Ms());
        String vector = Tr.t("search.info.vector", r.vectorCount(), r.vectorMs());
        return switch (m) {
            case BM25 -> bm25;
            case VECTOR -> vector;
            case HYBRID -> bm25 + " · " + vector + " · " + Tr.t("search.info.merged");
        };
    }

    private void loadSampleData() {
        setBusy(true, Tr.t("search.loadingData"));
        Background.run(this, loader::load, count -> {
            Notification.show(Tr.t("search.loaded", count)).addThemeVariants(NotificationVariant.SUCCESS);
            setBusy(false, null);
            items = List.of();
            checkHealth();
        }, this::failed);
    }

    private void confirmClear() {
        Paragraph text = new Paragraph(Tr.t("search.clearConfirm"));
        AppDialog dialog = new AppDialog(Tr.t("search.clear"), text);
        Button delete = new Button(Tr.t("search.delete"), e -> {
            setBusy(true, Tr.t("search.deleting"));
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
            Notification.show(Tr.t("common.unexpectedError", Background.message(e))).addThemeVariants(NotificationVariant.ERROR);
        }
    }

    private void setBusy(boolean isBusy, String text) {
        busy = isBusy;
        applyEnabled();
        if (text != null) resultInfo.setText(text);
    }

    @Override
    public String getPageTitle() {
        return Tr.t("page.search");
    }

    private record Shown(List<Hit> hits, HybridRetriever.Result result) {
    }
}
