package com.example.ragdemo.weaviate;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import com.example.ragdemo.rag.SystemDownException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.example.ragdemo.rag.ServiceStatus.Service.WEAVIATE;

/**
 * The schema of the {@value DemoDocCollection#NAME} collection, read live from Weaviate's REST API
 * ({@code GET /v1/schema/DemoDoc}) — for the 🧱 Schema dialog of 🔎 Search Data. It is exactly what Weaviate stores,
 * not what the app meant to create.
 */
@Component
public class WeaviateSchema {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** One property: name, data type, tokenization, and whether it is in the BM25 index / usable in filters. */
    public record Field(String name, String dataType, String tokenization, boolean searchable, boolean filterable) {
    }

    /**
     * The collection as Weaviate describes it. {@code url} is the REST address it was read from; {@code json} the
     * raw answer, pretty-printed.
     */
    public record Schema(String url, String collection, String description, String vectorizer, String vectorIndex,
                         List<String> vectorSources, List<Field> fields, String json) {
    }

    private final KeyStore keys;

    public WeaviateSchema(KeyStore keys) {
        this.keys = keys;
    }

    /** The live schema, or null when the collection does not exist yet. */
    public Schema fetch() {
        String key = keys.weaviateKey();
        if (keys.weaviateUrl().isBlank() || key.isBlank()) throw new SystemDownException(WEAVIATE, "status.weaviate.noKey");
        String url = WeaviateHealth.withScheme(keys.weaviateUrl()) + "/v1/schema/" + DemoDocCollection.NAME;
        try {
            HttpResponse<String> response = Http.get(url, key, Duration.ofSeconds(15));
            if (response.statusCode() == 404) return null;
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new SystemDownException(WEAVIATE, "status.invalidKey");
            }
            if (response.statusCode() != 200) throw new SystemDownException(WEAVIATE, "status.http", response.statusCode());
            return parse(url, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SystemDownException(WEAVIATE, "status.interrupted");
        } catch (SystemDownException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemDownException(WEAVIATE, "status.unreachable", Http.describe(e));
        }
    }

    /** The parts worth showing; works for named vectors ({@code vectorConfig}) and the older single-vector form. */
    static Schema parse(String url, String body) {
        JsonNode c = JSON.readTree(body);
        List<Field> fields = new ArrayList<>();
        for (JsonNode p : c.path("properties")) {
            fields.add(new Field(p.path("name").asString(""), join(p.path("dataType")),
                    p.path("tokenization").asString("—"), p.path("indexSearchable").asBoolean(false),
                    p.path("indexFilterable").asBoolean(false)));
        }
        String vectorizer = c.path("vectorizer").asString("");
        String index = c.path("vectorIndexType").asString("");
        List<String> sources = new ArrayList<>();
        JsonNode named = c.path("vectorConfig");
        if (named.isObject() && !named.isEmpty()) {
            JsonNode first = named.iterator().next();
            JsonNode v = first.path("vectorizer");
            if (v.isObject() && !v.isEmpty()) {
                String name = v.propertyNames().iterator().next();
                vectorizer = name;
                for (JsonNode s : v.path(name).path("sourceProperties")) sources.add(s.asString(""));
            }
            index = first.path("vectorIndexType").asString(index);
        }
        return new Schema(url, c.path("class").asString(DemoDocCollection.NAME), c.path("description").asString(""),
                vectorizer.isBlank() ? "—" : vectorizer, index.isBlank() ? "—" : index, sources, fields,
                JSON.writerWithDefaultPrettyPrinter().writeValueAsString(c));
    }

    private static String join(JsonNode array) {
        List<String> parts = new ArrayList<>();
        for (JsonNode n : array) parts.add(n.asString(""));
        return parts.isEmpty() ? "—" : String.join(", ", parts);
    }
}
