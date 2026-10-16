package com.example.ragdemo.db;

import com.example.ragdemo.keys.KeyStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:browser;DB_CLOSE_DELAY=-1")
class DatabaseBrowserTest {

    @Autowired
    DatabaseBrowser db;

    @Autowired
    KeyStore keys;

    @Test
    void listsTheAppTablesAndReadsRowsWithTheSqlShown() {
        keys.set(KeyStore.SYSTEM2_KEY, "sk-or-v1-secret-1234");
        keys.set(KeyStore.SYSTEM2_BASE_URL, "https://openrouter.ai/api/v1");

        assertThat(db.tables()).extracting(DatabaseBrowser.Table::name)
                .contains("API_SETTING", "CHAT_SESSION", "CHAT_MESSAGE");
        DatabaseBrowser.Page page = db.rows("api_setting", 0, 50);
        assertThat(page.sql()).isEqualTo("SELECT * FROM \"API_SETTING\" ORDER BY 1 DESC LIMIT 50 OFFSET 0");
        assertThat(page.rows()).anySatisfy(r -> assertThat(r.get("SETTING_VALUE")).isEqualTo("••••1234"));
        assertThat(page.rows()).anySatisfy(r -> assertThat(r.get("SETTING_VALUE")).isEqualTo("https://openrouter.ai/api/v1"));
        assertThat(page.rows()).noneSatisfy(r -> assertThat(String.valueOf(r.get("SETTING_VALUE"))).contains("secret"));
    }

    @Test
    void onlyRealTablesCanBeRead() {
        assertThatThrownBy(() -> db.rows("API_SETTING; DROP TABLE CHAT_MESSAGE", 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> db.count("NO_SUCH_TABLE")).isInstanceOf(IllegalArgumentException.class);
        assertThat(db.tables()).extracting(DatabaseBrowser.Table::name).contains("CHAT_MESSAGE");   // still there
    }

    @Test
    void aRowCanBeChangedAndDeleted_byItsPrimaryKey() {
        keys.set(KeyStore.SYSTEM1_URL, "https://old.example");
        var row = db.rows("API_SETTING", 0, 50).rows().stream()
                .filter(r -> KeyStore.SYSTEM1_URL.equals(r.get("NAME"))).findFirst().orElseThrow();
        assertThat(db.primaryKey("API_SETTING")).containsExactly("NAME");

        assertThat(db.update("API_SETTING", row, java.util.Map.of("SETTING_VALUE", "https://new.example"))).isEqualTo(1);
        assertThat(keys.system1Url()).isEqualTo("https://new.example");   // the app reads the changed value

        assertThat(db.delete("API_SETTING", row)).isEqualTo(1);
        assertThat(keys.system1Url()).isEqualTo(KeyStore.DEFAULT_SYSTEM1_URL);   // gone → the default again
    }

    @Test
    void keysAndPrimaryKeysCannotBeEdited() {
        keys.set(KeyStore.WEAVIATE_KEY, "weaviate-secret-9999");
        var row = db.rows("API_SETTING", 0, 50).rows().stream()
                .filter(r -> KeyStore.WEAVIATE_KEY.equals(r.get("NAME"))).findFirst().orElseThrow();
        assertThat(db.locked("API_SETTING", row, "SETTING_VALUE")).isTrue();
        assertThatThrownBy(() -> db.update("API_SETTING", row, java.util.Map.of("SETTING_VALUE", "x")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> db.update("API_SETTING", row, java.util.Map.of("NAME", "other")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> db.update("API_SETTING", row, java.util.Map.of("NO_SUCH_COLUMN", "x")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(keys.weaviateKey()).isEqualTo("weaviate-secret-9999");   // untouched
    }
}
