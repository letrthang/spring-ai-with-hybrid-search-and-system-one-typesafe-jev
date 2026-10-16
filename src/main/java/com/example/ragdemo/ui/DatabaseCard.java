package com.example.ragdemo.ui;

import com.example.ragdemo.db.DatabaseBrowser;
import com.example.ragdemo.db.DatabaseBrowser.Table;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 💾 App database (H2) — under the Search Data grid: what the app itself saved (chat sessions, messages, settings),
 * read live with plain SQL, keys masked. Pick a table → its rows, newest first, one page at a time (lazy grid);
 * double-click or right-click a row → an edit form (💾 save the changed columns, 🗑 delete the row).
 */
public class DatabaseCard extends Div {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final int SHORT = 90;

    private final DatabaseBrowser db;
    private final Select<Table> table = new Select<>();
    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final Span count = new Span();
    private final Span sql = new Span();

    public DatabaseCard(DatabaseBrowser db) {
        this.db = db;
        addClassNames("rd-card", "p-4", "flex", "flex-col", "gap-3");

        H3 title = new H3(Tr.t("db.title"));
        title.addClassNames("m-0", "text-base");
        Span lead = new Span(Tr.t("db.lead"));
        lead.addClassNames("rd-muted", "text-sm");

        table.setLabel(Tr.t("db.table"));
        table.setWidth("18rem");
        table.setItemLabelGenerator(t -> t.name() + " (" + t.rows() + ")");
        table.addValueChangeListener(e -> {
            if (e.getValue() != null) show(e.getValue());
        });
        Button refresh = new Button(Tr.t("db.refresh"), e -> loadTables());
        refresh.addThemeVariants(ButtonVariant.TERTIARY);
        count.addClassNames("rd-status", "rd-status-UNKNOWN", "text-xs");
        Div bar = new Div(table, refresh, count);
        bar.addClassNames("flex", "flex-wrap", "items-end", "gap-3");
        count.getStyle().set("margin-bottom", "8px");   // on the line of the picker, not of its label

        sql.addClassNames("rd-wv-mono", "rd-muted", "break-all");

        grid.addThemeVariants(GridVariant.ROW_STRIPES);
        grid.setEmptyStateText(Tr.t("db.empty"));
        grid.setHeight("360px");
        grid.addClassName("rd-db-grid");
        grid.addItemDoubleClickListener(e -> openRow(e.getItem()));
        GridContextMenu<Map<String, Object>> menu = grid.addContextMenu();
        menu.setDynamicContentHandler(row -> row != null);
        menu.addItem(Tr.t("common.details"), e -> e.getItem().ifPresent(this::openRow));

        // ⛶ full screen (Esc or ⛶ again to leave), like the Passages grid
        Button fullScreen = new Button("⛶", e -> toggleFullScreen());
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.getElement().setAttribute("title", Tr.t("common.fullScreenEsc"));
        fullScreen.getElement().setAttribute("aria-label", Tr.t("common.fullScreen"));
        Div head = new Div(InfoHint.with(title, Hint.DATABASE), fullScreen);
        head.addClassNames("flex", "justify-between", "items-center");
        Shortcuts.addShortcutListener(this, () -> {
            if (hasClassName("rd-fullscreen")) toggleFullScreen();
        }, Key.ESCAPE).listenOn(this);

        add(head, lead, bar, sql, grid);
        addAttachListener(e -> {
            if (e.isInitialAttach()) loadTables();
        });
    }

    private void toggleFullScreen() {
        if (hasClassName("rd-fullscreen")) removeClassName("rd-fullscreen");
        else addClassName("rd-fullscreen");
    }

    /** The tables with their row counts (background), then the first one is shown. */
    private void loadTables() {
        Table current = table.getValue();
        Background.run(this, db::tables, tables -> {
            table.setItems(tables);
            Table pick = tables.stream().filter(t -> current != null && t.name().equals(current.name())).findFirst()
                    .orElse(tables.stream().filter(t -> t.name().equals("CHAT_MESSAGE")).findFirst()
                            .orElse(tables.isEmpty() ? null : tables.getFirst()));
            if (pick != null) {
                table.setValue(pick);
                show(pick);
            }
        });
    }

    /** One table in the grid: a column per database column, rows fetched one page at a time. */
    private void show(Table t) {
        grid.removeAllColumns();
        for (String column : t.columns()) {
            grid.addColumn(row -> shorten(row.get(column))).setHeader(column).setResizable(true).setAutoWidth(true)
                    .setFlexGrow(column.contains("TEXT") || column.contains("JSON") || column.contains("TITLE") ? 3 : 1);
        }
        count.setText(Tr.t("db.rows", t.rows()));
        sql.setText("SQL: " + DatabaseBrowser.sql(t.name(), 0, 50));   // shown at once, also for an empty table
        grid.setItems(q -> {
            DatabaseBrowser.Page page = db.rows(t.name(), q.getOffset(), q.getLimit());
            sql.setText("SQL: " + page.sql());
            return page.rows().stream();
        }, q -> (int) db.count(t.name()));
    }

    /**
     * One row as an edit form: locked cells (primary key, a key's masked value) read-only, the others editable; JSON
     * in a text area. 💾 saves only the changed columns; 🗑 deletes the row after a confirmation. The SQL that runs is
     * shown in the form.
     */
    private void openRow(Map<String, Object> row) {
        Table t = table.getValue();
        if (t == null) return;
        String name = t.name();
        List<String> pk = db.primaryKey(name);
        Map<String, HasValue<?, String>> inputs = new LinkedHashMap<>();
        Map<String, String> original = new LinkedHashMap<>();
        Div form = new Div();
        form.addClassNames("flex", "flex-col", "gap-2");
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String column = e.getKey();
            String value = e.getValue() == null ? "" : String.valueOf(e.getValue());
            boolean json = value.startsWith("{") || value.startsWith("[");
            String shown = json ? pretty(value) : value;
            boolean locked = db.locked(name, row, column);
            Component input;
            if (json || shown.length() > 120) {
                TextArea area = new TextArea(column, shown, "");
                area.setValue(shown);
                area.setMaxHeight("16rem");
                area.addClassName("rd-db-json");
                area.setReadOnly(locked);
                inputs.put(column, area);
                input = area;
            } else {
                TextField field = new TextField(column);
                field.setValue(shown);
                field.setReadOnly(locked);
                inputs.put(column, field);
                input = field;
            }
            if (locked) {
                ((HasHelper) input).setHelperText(pk.contains(column) ? Tr.t("db.lockedKey") : Tr.t("db.lockedSecret"));
            }
            ((HasSize) input).setWidthFull();
            original.put(column, shown);
            form.add(input);
        }

        Span preview = new Span();
        preview.addClassNames("rd-wv-mono", "rd-muted", "break-all");
        Runnable showSql = () -> {
            List<String> changed = changed(inputs, original).keySet().stream().toList();
            preview.setText(changed.isEmpty() ? Tr.t("db.noChanges")
                    : "SQL: " + DatabaseBrowser.updateSql(name, changed, pk));
        };
        inputs.values().forEach(i -> i.addValueChangeListener(e -> showSql.run()));
        showSql.run();

        AppDialog dialog = new AppDialog("✏️ " + name, form, preview);
        dialog.setFixedHeight("min(85vh, 820px)");
        Button save = new Button(Tr.t("db.save"), e -> {
            Map<String, String> changes = changed(inputs, original);
            if (changes.isEmpty()) return;
            try {
                int n = db.update(name, row, changes);
                Notification.show(Tr.t("db.saved", n)).addThemeVariants(NotificationVariant.SUCCESS);
                dialog.close();
                refresh();
            } catch (RuntimeException ex) {
                Notification.show("⚠️ " + Background.message(rootCause(ex))).addThemeVariants(NotificationVariant.ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.PRIMARY);
        Button delete = new Button(Tr.t("db.delete"), e -> confirmDelete(name, row, pk, dialog));
        delete.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.TERTIARY);
        save.setEnabled(!pk.isEmpty());
        delete.setEnabled(!pk.isEmpty());
        dialog.getFooter().add(delete, save);
        dialog.open();
    }

    private void confirmDelete(String name, Map<String, Object> row, List<String> pk, AppDialog editor) {
        Span sqlText = new Span("SQL: " + DatabaseBrowser.deleteSql(name, pk));
        sqlText.addClassNames("rd-wv-mono", "rd-muted", "break-all");
        Paragraph question = new Paragraph(Tr.t("db.deleteConfirm", name));
        question.addClassName("m-0");
        AppDialog confirm = new AppDialog(Tr.t("db.delete"), question, sqlText);
        confirm.setWidth("min(520px, 100vw)");
        Button yes = new Button(Tr.t("db.delete"), e -> {
            try {
                int n = db.delete(name, row);
                Notification.show(Tr.t("db.deleted", n)).addThemeVariants(NotificationVariant.SUCCESS);
                confirm.close();
                editor.close();
                refresh();
            } catch (RuntimeException ex) {
                Notification.show("⚠️ " + Background.message(rootCause(ex))).addThemeVariants(NotificationVariant.ERROR);
            }
        });
        yes.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.PRIMARY);
        confirm.getFooter().add(yes);
        confirm.open();
    }

    /** The columns whose text differs from what was shown. */
    private static Map<String, String> changed(Map<String, HasValue<?, String>> inputs, Map<String, String> original) {
        Map<String, String> changes = new LinkedHashMap<>();
        inputs.forEach((column, input) -> {
            if (!input.isReadOnly() && !input.getValue().equals(original.get(column))) {
                changes.put(column, input.getValue());
            }
        });
        return changes;
    }

    /** After a change: the grid and the row counts again. */
    private void refresh() {
        grid.getDataProvider().refreshAll();
        loadTables();
    }

    private static Throwable rootCause(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t;
    }

    private static String shorten(Object value) {
        if (value == null) return "—";
        String s = value.toString().replaceAll("\\s+", " ");
        return s.length() <= SHORT ? s : s.substring(0, SHORT) + "… (" + s.length() + ")";
    }

    private static String pretty(String json) {
        try {
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(JSON.readTree(json));
        } catch (RuntimeException notJson) {
            return json;
        }
    }

    /** For tests: the column headers in order. */
    List<String> headers() {
        return grid.getColumns().stream().map(c -> c.getHeaderText()).toList();
    }
}
