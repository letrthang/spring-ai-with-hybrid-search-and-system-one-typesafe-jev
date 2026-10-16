package com.example.ragdemo.db;

import com.example.ragdemo.keys.KeyStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A look into the app's own H2 database (🔎 Search Data → 💾 App database): which tables there are, their rows one
 * page at a time — plain SQL, so people see what JPA stored (chat sessions, messages, settings) — and a row can be
 * changed or deleted.
 *
 * <ul>
 *   <li>Only tables that exist in {@code INFORMATION_SCHEMA} (schema PUBLIC) can be read; the name is checked, then
 *       quoted — no SQL comes from the browser.</li>
 *   <li>Key values are never shown: in the settings table a row whose name contains "key" shows {@code ••••abcd}.</li>
 *   <li>Changes are fixed, parameterised statements: {@code UPDATE "T" SET "C" = ? WHERE "PK" = ?} and
 *       {@code DELETE FROM "T" WHERE "PK" = ?} — column names checked against INFORMATION_SCHEMA, the row found by
 *       its primary key, values passed as parameters. No free SQL.</li>
 *   <li>A key's value can never be read or changed here (use 🔑 Key Management).</li>
 * </ul>
 */
@Component
public class DatabaseBrowser {

    /** The settings table and its columns (ApiSetting): values of names containing "key" are masked. */
    static final String SETTINGS_TABLE = "API_SETTING";

    /** One table: its name, its columns in order, and how many rows it has. */
    public record Table(String name, List<String> columns, long rows) {
    }

    /** One page of rows, and the exact SQL that read it (shown to the user). */
    public record Page(List<Map<String, Object>> rows, String sql) {
    }

    private final JdbcTemplate jdbc;

    public DatabaseBrowser(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The app's tables (schema PUBLIC), with their columns and row counts. */
    public List<Table> tables() {
        List<String> names = jdbc.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME",
                String.class);
        List<Table> tables = new ArrayList<>();
        for (String name : names) {
            List<String> columns = jdbc.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                    + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION", String.class, name);
            Long rows = jdbc.queryForObject("SELECT COUNT(*) FROM " + quote(name), Long.class);
            tables.add(new Table(name, columns, rows == null ? 0 : rows));
        }
        return tables;
    }

    /** The SQL that reads one page of a table — what the card shows above the grid. */
    public static String sql(String table, int offset, int limit) {
        return "SELECT * FROM " + quote(table) + " ORDER BY 1 DESC LIMIT " + Math.max(1, Math.min(limit, 200))
                + " OFFSET " + Math.max(0, offset);
    }

    /** Rows {@code offset … offset+limit} of a table, newest first (by its first column), keys masked. */
    public Page rows(String table, int offset, int limit) {
        String name = known(table);
        String sql = sql(name, offset, limit);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList(sql)) rows.add(masked(name, row));
        return new Page(rows, sql);
    }

    public long count(String table) {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM " + quote(known(table)), Long.class);
        return n == null ? 0 : n;
    }

    /** The primary-key columns of a table, in order (empty if it has none — then rows cannot be changed). */
    public List<String> primaryKey(String table) {
        return jdbc.queryForList("SELECT k.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS c "
                + "JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE k ON k.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA "
                + "AND k.CONSTRAINT_NAME = c.CONSTRAINT_NAME "
                + "WHERE c.TABLE_SCHEMA = 'PUBLIC' AND c.TABLE_NAME = ? AND c.CONSTRAINT_TYPE = 'PRIMARY KEY' "
                + "ORDER BY k.ORDINAL_POSITION", String.class, known(table));
    }

    /** True for a cell that must not be edited here: a primary-key column, or a key's (masked) value. */
    public boolean locked(String table, Map<String, Object> row, String column) {
        return primaryKey(table).contains(column) || isKeyValue(known(table), row, column);
    }

    /**
     * Saves the changed columns of one row: {@code UPDATE "T" SET "C1" = ?, … WHERE "PK" = ?}. Returns the number of
     * rows changed (1, or 0 if the row is gone). Locked cells are refused.
     */
    public int update(String table, Map<String, Object> row, Map<String, String> changes) {
        String name = known(table);
        if (changes.isEmpty()) return 0;
        List<String> pk = keyOf(name);
        List<String> columns = columns(name);
        List<String> sets = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        for (Map.Entry<String, String> c : changes.entrySet()) {
            String column = c.getKey().toUpperCase(Locale.ROOT);
            if (!columns.contains(column)) throw new IllegalArgumentException("unknown column: " + c.getKey());
            if (pk.contains(column) || isKeyValue(name, row, column)) {
                throw new IllegalArgumentException("locked column: " + column);
            }
            sets.add(quote(column) + " = ?");
            args.add(c.getValue() == null || c.getValue().isEmpty() ? null : c.getValue());
        }
        for (String k : pk) args.add(row.get(k));
        return jdbc.update("UPDATE " + quote(name) + " SET " + String.join(", ", sets) + " WHERE " + where(pk),
                args.toArray());
    }

    /** Deletes one row by its primary key: {@code DELETE FROM "T" WHERE "PK" = ?}. */
    public int delete(String table, Map<String, Object> row) {
        String name = known(table);
        List<String> pk = keyOf(name);
        return jdbc.update("DELETE FROM " + quote(name) + " WHERE " + where(pk),
                pk.stream().map(row::get).toArray());
    }

    /** The statement {@link #update} will run, for showing it (values as ?). */
    public static String updateSql(String table, List<String> columns, List<String> pk) {
        return "UPDATE " + quote(table) + " SET " + String.join(", ", columns.stream().map(c -> quote(c) + " = ?").toList())
                + " WHERE " + where(pk);
    }

    public static String deleteSql(String table, List<String> pk) {
        return "DELETE FROM " + quote(table) + " WHERE " + where(pk);
    }

    private List<String> keyOf(String name) {
        List<String> pk = primaryKey(name);
        if (pk.isEmpty()) throw new IllegalArgumentException("table has no primary key: " + name);
        return pk;
    }

    private List<String> columns(String name) {
        return jdbc.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ?", String.class, name);
    }

    private static String where(List<String> pk) {
        return String.join(" AND ", pk.stream().map(k -> quote(k) + " = ?").toList());
    }

    /** The value column of a settings row whose name contains "key". */
    static boolean isKeyValue(String table, Map<String, Object> row, String column) {
        if (!SETTINGS_TABLE.equals(table) || !"SETTING_VALUE".equals(column)) return false;
        Object name = row.get("NAME");
        return name != null && name.toString().toLowerCase(Locale.ROOT).contains("key");
    }

    /** The table name exactly as INFORMATION_SCHEMA has it — anything else is refused. */
    String known(String table) {
        String upper = table == null ? "" : table.toUpperCase(Locale.ROOT);
        Integer found = jdbc.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = ?", Integer.class, upper);
        if (found == null || found == 0) throw new IllegalArgumentException("unknown table: " + table);
        return upper;
    }

    private static String quote(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    /** In the settings table, a key's value becomes ••••abcd (the name column says which setting it is). */
    static Map<String, Object> masked(String table, Map<String, Object> row) {
        Map<String, Object> copy = new LinkedHashMap<>(row);
        if (!SETTINGS_TABLE.equals(table)) return copy;
        Object name = copy.get("NAME");
        if (name != null && name.toString().toLowerCase(Locale.ROOT).contains("key")) {
            copy.computeIfPresent("SETTING_VALUE", (k, v) -> KeyStore.mask(String.valueOf(v)));
        }
        return copy;
    }
}
