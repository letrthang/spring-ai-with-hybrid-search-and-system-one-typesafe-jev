package com.example.ragdemo.weaviate;

import com.example.ragdemo.keys.KeyStore;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reads one stored passage exactly as Weaviate keeps it — for the 🔍 Details dialog of 🔎 Search Data, so people can
 * see where a record lives (cluster → collection → object UUID), its fields, and the vector Weaviate made for it.
 */
@Component
public class WeaviateRecords {

    /** One Weaviate object. {@code vector} is the embedding Weaviate's vectorizer stored for it (may be null). */
    public record StoredRecord(String cluster, String collection, String uuid, Map<String, Object> properties,
                               Long createdAt, Long updatedAt, float[] vector) {
    }

    private final KeyStore keys;
    private final WeaviateConnector weaviate;

    public WeaviateRecords(KeyStore keys, WeaviateConnector weaviate) {
        this.keys = keys;
        this.weaviate = weaviate;
    }

    /** The object with this UUID, or null if it no longer exists. */
    public StoredRecord fetch(String uuid) {
        return weaviate.call("fetch record", client -> {
            var found = client.collections.use(DemoDocCollection.NAME).query
                    .fetchObjectById(uuid, q -> q.includeVector()
                            .returnMetadata(Metadata.CREATION_TIME_UNIX, Metadata.LAST_UPDATE_TIME_UNIX));
            return found.map(this::toRecord).orElse(null);
        });
    }

    private StoredRecord toRecord(WeaviateObject<Map<String, Object>> o) {
        float[] vector = null;
        if (o.vectors() != null) {
            try {
                vector = o.vectors().getDefaultSingle();
            } catch (RuntimeException named) {
                // a named vector: take the first one
                vector = o.vectors().asMap().values().stream().filter(v -> v instanceof float[])
                        .map(v -> (float[]) v).findFirst().orElse(null);
            }
        }
        // the site owner's default cluster host is masked; the REST address is still readable as a lesson
        String cluster = keys.shownUrl(KeyStore.WEAVIATE_URL, WeaviateHealth.withScheme(keys.weaviateUrl()));
        return new StoredRecord(cluster, DemoDocCollection.NAME, o.uuid(),
                o.properties() == null ? Map.of() : o.properties(), o.createdAt(), o.lastUpdatedAt(), vector);
    }
}
