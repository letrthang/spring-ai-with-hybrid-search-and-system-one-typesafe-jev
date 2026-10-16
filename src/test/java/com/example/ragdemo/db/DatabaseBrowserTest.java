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
    void listsTheAppTablesAndReadsRowsWithTheSqlShown_noKeyIsEverInTheDatabase() {
        keys.set(KeyStore.SYSTEM2_KEY, "sk-or-v1-secret-1234");   // kept in memory only
        keys.set(KeyStore.SYSTEM2_MODELS, "a:free\nb:free");

        assertThat(db.tables()).extracting(DatabaseBrowser.Table::name)
                .contains("API_SETTING", "CHAT_SESSION", "CHAT_MESSAGE");
        DatabaseBrowser.Page page = db.rows("api_setting", 0, 50);
        assertThat(page.sql()).isEqualTo("SELECT * FROM \"API_SETTING\" ORDER BY 1 DESC LIMIT 50 OFFSET 0");
        assertThat(page.rows()).extracting(r -> r.get("NAME")).contains(KeyStore.SYSTEM2_MODELS)
                .doesNotContain(KeyStore.SYSTEM2_KEY, KeyStore.WEAVIATE_KEY, KeyStore.WEAVIATE_URL);
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
    void aRowCanBeChangedAndDeleted_byItsPrimaryKey_butNotItsKey() {
        keys.set(KeyStore.SYSTEM2_MODELS, "a:free");
        var row = db.rows("API_SETTING", 0, 50).rows().stream()
                .filter(r -> KeyStore.SYSTEM2_MODELS.equals(r.get("NAME"))).findFirst().orElseThrow();
        assertThat(db.primaryKey("API_SETTING")).containsExactly("NAME");
        assertThat(db.locked("API_SETTING", row, "NAME")).isTrue();
        assertThatThrownBy(() -> db.update("API_SETTING", row, java.util.Map.of("NAME", "other")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> db.update("API_SETTING", row, java.util.Map.of("NO_SUCH_COLUMN", "x")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(db.update("API_SETTING", row, java.util.Map.of("SETTING_VALUE", "b:free"))).isEqualTo(1);
        assertThat(keys.system2Models()).containsExactly("b:free");   // the app reads the changed value

        assertThat(db.delete("API_SETTING", row)).isEqualTo(1);
        assertThat(keys.system2Models()).isEqualTo(KeyStore.DEFAULT_SYSTEM2_MODELS);   // gone → the default again
    }
}
