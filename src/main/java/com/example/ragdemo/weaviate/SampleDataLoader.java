package com.example.ragdemo.weaviate;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.InsertManyResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Loads {@code src/main/resources/sample-data/*.md} into the {@value DemoDocCollection#NAME} collection.
 *
 * <p><b>Chunking:</b> one passage per {@code ## section} of a Markdown file. That is the simplest splitter that keeps
 * each passage about one topic. (Spring AI's {@code TokenTextSplitter} splits by token count instead; for hand-written
 * docs with headings, the headings are the better boundary.)
 *
 * <p>Each passage gets a fixed id made from its file and section number, so loading twice <b>replaces</b> the
 * passages instead of duplicating them.
 */
@Component
public class SampleDataLoader {

    /** One passage. */
    public record Chunk(String source, int chunkNo, String title, String body) {

        public String id() {
            return UUID.nameUUIDFromBytes((source + "#" + chunkNo).getBytes(StandardCharsets.UTF_8)).toString();
        }

        Map<String, Object> properties() {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("title", title);
            p.put("body", body);
            p.put("source", source);
            p.put("chunkNo", chunkNo);
            return p;
        }
    }

    private final WeaviateConnector weaviate;

    public SampleDataLoader(WeaviateConnector weaviate) {
        this.weaviate = weaviate;
    }

    /** Creates the collection if needed and inserts every sample passage. Returns the number of passages. */
    public int load() {
        List<Chunk> chunks = readSampleData();
        return weaviate.call("load sample data", client -> {
            DemoDocCollection.ensure(client);
            List<WeaviateObject<Map<String, Object>>> objects = new ArrayList<>();
            for (Chunk chunk : chunks) {
                objects.add(WeaviateObject.of(o -> o.uuid(chunk.id()).properties(chunk.properties())));
            }
            InsertManyResponse response = client.collections.use(DemoDocCollection.NAME).data.insertMany(objects);
            if (!response.errors().isEmpty()) {
                throw new IllegalStateException(response.errors().size() + " passages failed: "
                        + response.errors().getFirst());
            }
            return chunks.size();
        });
    }

    /** Deletes the whole collection (Search Data → 🗑 Clear collection). */
    public void deleteCollection() {
        weaviate.call("delete collection", client -> {
            if (client.collections.exists(DemoDocCollection.NAME)) client.collections.delete(DemoDocCollection.NAME);
            return null;
        });
    }

    /** Every {@code sample-data/*.md} on the classpath, split into passages, files in name order. */
    public static List<Chunk> readSampleData() {
        try {
            Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath:sample-data/*.md");
            Arrays.sort(files, Comparator.comparing(Resource::getFilename));
            List<Chunk> chunks = new ArrayList<>();
            for (Resource file : files) {
                chunks.addAll(split(file.getFilename(), file.getContentAsString(StandardCharsets.UTF_8)));
            }
            return chunks;
        } catch (IOException e) {
            throw new IllegalStateException("cannot read sample-data", e);
        }
    }

    /**
     * Splits one Markdown file at its {@code ## } headings. The passage title is "{document title} — {section}", so
     * the passage still makes sense when it is read alone.
     */
    static List<Chunk> split(String source, String markdown) {
        String docTitle = source;
        List<Chunk> chunks = new ArrayList<>();
        String section = null;
        StringBuilder body = new StringBuilder();
        for (String line : markdown.split("\\R")) {
            if (line.startsWith("# ")) {
                docTitle = line.substring(2).strip();
            } else if (line.startsWith("## ")) {
                addChunk(chunks, source, docTitle, section, body);
                section = line.substring(3).strip();
                body.setLength(0);
            } else if (section != null) {
                body.append(line).append('\n');
            }
        }
        addChunk(chunks, source, docTitle, section, body);
        return chunks;
    }

    private static void addChunk(List<Chunk> chunks, String source, String docTitle, String section,
                                 StringBuilder body) {
        if (section == null || body.toString().isBlank()) return;
        chunks.add(new Chunk(source, chunks.size() + 1, docTitle + " — " + section, unwrap(body.toString())));
    }

    /** Joins the lines of a wrapped Markdown paragraph; keeps blank lines and "- " list items on their own line. */
    static String unwrap(String text) {
        List<String> lines = new ArrayList<>();
        for (String raw : text.strip().split("\\R")) {
            String line = raw.strip();
            boolean continues = !line.isEmpty() && !line.startsWith("- ") && !lines.isEmpty() && !lines.getLast().isEmpty();
            if (continues) {
                lines.set(lines.size() - 1, lines.getLast() + " " + line);
            } else {
                lines.add(line);
            }
        }
        return String.join("\n", lines);
    }
}
