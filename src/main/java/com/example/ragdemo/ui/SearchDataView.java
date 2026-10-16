package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.SystemDownException;
import com.example.ragdemo.weaviate.HybridRetriever;
import com.example.ragdemo.weaviate.HybridRetriever.Hit;
import com.example.ragdemo.weaviate.HybridRetriever.Mode;
import com.example.ragdemo.weaviate.SampleDataLoader;
import com.example.ragdemo.weaviate.WeaviateHealth;
import com.example.ragdemo.db.DatabaseBrowser;
import com.example.ragdemo.weaviate.LearnedAnswers;
import com.example.ragdemo.weaviate.WeaviateRecords;
import com.example.ragdemo.weaviate.WeaviateSchema;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 🔎 Search Data: look inside Weaviate. The same question in the three modes shows <i>why</i> hybrid search exists:
 * BM25 and vector search rank the passages differently, and RRF merges both.
 *
 * <p>An empty query lists every passage <b>lazily</b>: the grid asks Weaviate for one page at a time as you scroll
 * (3,000 sample passages). A search shows at most {@value #SEARCH_LIMIT} hits, all at once (RRF needs both full
 * lists). Searches and the count run on a virtual thread; the grid's page requests are Vaadin's own lazy loading.
 */
@Route("search")
public class SearchDataView extends Div implements HasDynamicTitle {

    static final int SEARCH_LIMIT = 40;   // 20 BM25 + 20 vector at most, fewer after merging

    private final HybridRetriever retriever;
    private final SampleDataLoader loader;
    private final WeaviateHealth health;
    private final WeaviateRecords records;
    private final WeaviateSchema schemas;
    private Grid.Column<Hit> bm25Column;
    private Grid.Column<Hit> vectorColumn;
    private Grid.Column<Hit> rrfColumn;

    private final TextField query = new TextField();
    private final RadioButtonGroup<Mode> mode = new RadioButtonGroup<>();
    private final Button searchButton = new Button(Tr.t("search.search"));
    private final Button loadButton = new Button(Tr.t("search.load"));
    private final Button clearButton = new Button(Tr.t("search.clear"));
    private final Button schemaButton = new Button(Tr.t("schema.button"));
    private final StatusPill pill = new StatusPill(ServiceStatus.Service.WEAVIATE);
    private final Paragraph warning = new Paragraph();
    private final Span resultInfo = new Span();
    private final Grid<Hit> grid = new Grid<>();
    private final SplitMasterDetail masterDetail = new SplitMasterDetail(grid);
    private final Div gridCard = new Div();
    private List<Hit> items = List.of();
    /** Row number per passage id, for the # column (search results, or the pages loaded so far). */
    private final Map<String, Integer> position = new HashMap<>();
    private boolean showing;   // the grid shows a list or a search result (else: nothing loaded yet)
    private ServiceStatus status;
    private boolean busy;

    public SearchDataView(HybridRetriever retriever, SampleDataLoader loader, WeaviateHealth health,
                          WeaviateRecords records, WeaviateSchema schemas,
                          DatabaseBrowser database, LearnedAnswers learned) {
        this.retriever = retriever;
        this.loader = loader;
        this.health = health;
        this.records = records;
        this.schemas = schemas;
        addClassNames("p-4", "flex", "flex-col", "gap-3", "h-full", "box-border");

        Paragraph intro = new Paragraph(Tr.t("search.intro"));
        intro.addClassNames("rd-muted", "m-0");
        warning.addClassNames("rd-warning", "m-0", "p-3");
        warning.setVisible(false);

        DatabaseCard databaseCard = new DatabaseCard(database);   // 💾 what the app saved in H2
        databaseCard.addClassName("shrink-0");
        LearnedCard learnedCard = new LearnedCard(learned);       // 🧠 what the app learned (Weaviate)
        learnedCard.addClassName("shrink-0");
        add(InfoHint.with(new H2(Tr.t("search.title")), Hint.WEAVIATE), intro, toolbar(), statusRow(), warning, results(),
                learnedCard, databaseCard);
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
        schemaButton.addThemeVariants(ButtonVariant.TERTIARY);
        schemaButton.addClickListener(e -> new SchemaDialog(schemas).open());   // GET /v1/schema/DemoDoc, live
        Div actions = new Div(mode, new InfoHint(Hint.SEARCH_MODES), loadButton, new InfoHint(Hint.CHUNKS),
                schemaButton, new InfoHint(Hint.SCHEMA), clearButton);
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
        grid.addColumn(h -> position.getOrDefault(h.id(), 0)).setHeader(InfoHint.with(new Span("#"), Hint.ROW_ORDER)).setWidth("4em").setFlexGrow(0)
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
                        detailBody(e.getValue()), RecordDialog.schemaOf(e.getValue()))));
        grid.addItemDoubleClickListener(e -> openRecord(e.getItem()));
        // right click on a row: the same 🔍 Details… dialog as a double-click
        GridContextMenu<Hit> menu = grid.addContextMenu();
        menu.setDynamicContentHandler(hit -> hit != null);   // only on rows, not on the header or empty space
        menu.addItem(Tr.t("common.details"), e -> e.getItem().ifPresent(this::openRecord));

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


    private void openRecord(Hit hit) {
        grid.select(hit);
        new RecordDialog(hit, records, detailBody(hit)).open();
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
        if (status.isReady() && !showing) search();
    }

    /** Search needs a ready collection; Load / Clear need only a reachable Weaviate. Nothing while busy. */
    private void applyEnabled() {
        boolean ready = status != null && status.isReady();
        boolean reachable = status != null && status.state() != ServiceStatus.State.DOWN;
        query.setEnabled(ready);
        searchButton.setEnabled(ready && !busy);
        loadButton.setEnabled(reachable && !busy);
        clearButton.setEnabled(reachable && !busy);
        schemaButton.setEnabled(reachable);
    }

    private void search() {
        String text = query.getValue().strip();
        Mode m = mode.getValue();
        setBusy(true, Tr.t(text.isEmpty() ? "search.loading" : "search.searching"));
        if (text.isEmpty()) {
            listAll();
            return;
        }
        Background.run(this, () -> retriever.search(text, m, SEARCH_LIMIT), result -> {
            // texts are built here, on the page (in its language), not on the background thread
            items = result.hits();
            position.clear();
            for (int i = 0; i < items.size(); i++) position.put(items.get(i).id(), i + 1);
            grid.setItems(items);
            showing = true;
            showScoreColumns(m);
            resultInfo.setText(Tr.t("search.info.search", items.size(), describe(result, m)));
            setBusy(false, null);
        }, this::failed);
    }

    /** Every passage, one page at a time: the count first (background), then Weaviate pages as the grid scrolls. */
    private void listAll() {
        Background.run(this, retriever::count, total -> {
            items = List.of();
            position.clear();
            grid.setItems(q -> page(q.getOffset(), q.getLimit()), q -> total.intValue());
            showing = true;
            showScoreColumns(null);
            resultInfo.setText(Tr.t("search.info.list", total));
            setBusy(false, null);
        }, this::failed);
    }

    private Stream<Hit> page(int offset, int limit) {
        List<Hit> page = retriever.list(offset, limit);
        for (int i = 0; i < page.size(); i++) position.put(page.get(i).id(), offset + i + 1);
        return page.stream();
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
        String scores = switch (m) {
            case BM25 -> bm25;
            case VECTOR -> vector;
            case HYBRID -> bm25 + " · " + vector + " · " + Tr.t("search.info.merged");
        };
        return scores + " · " + Tr.t("search.info.lang", r.lang());
    }

    private void loadSampleData() {
        setBusy(true, Tr.t("search.loadingData"));
        Background.run(this, loader::load, count -> {
            Notification.show(Tr.t("search.loaded", count)).addThemeVariants(NotificationVariant.SUCCESS);
            setBusy(false, null);
            showing = false;   // the health check lists the fresh data
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
                position.clear();
                grid.setItems(items);
                showing = false;
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

}
