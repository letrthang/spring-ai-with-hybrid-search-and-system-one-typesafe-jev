package com.example.ragdemo.weaviate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
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
 * Loads {@code src/main/resources/sample-data/{en,vi,ja}/*.md} into the {@value DemoDocCollection#NAME} collection —
 * the same topics in English, Vietnamese and Japanese, one folder per language.
 *
 * <p><b>Chunking:</b> one passage per {@code ## section} of a Markdown file. That is the simplest splitter that keeps
 * each passage about one topic. (Spring AI's {@code TokenTextSplitter} splits by token count instead; for hand-written
 * docs with headings, the headings are the better boundary.)
 *
 * <p>Loading <b>starts fresh</b>: the collection is dropped and created again, so the collection always holds exactly
 * the current sample data (no passages left over from an older version). Each passage also gets a fixed id made from
 * its file and section number.
 */
@Component
public class SampleDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    /** Passages per insert call: Weaviate makes a vector for each one, so a few hundred at a time stays fast. */
    static final int BATCH = 200;

    /** One passage. {@code source} is "lang/file.md", e.g. "vi/02-pcr.md". */
    public record Chunk(String source, int chunkNo, String title, String body) {

        /** "en", "vi" or "ja" — the folder the file is in. */
        public String lang() {
            int slash = source.indexOf('/');
            return slash > 0 ? source.substring(0, slash) : Lang.EN;
        }


        public String id() {
            return UUID.nameUUIDFromBytes((source + "#" + chunkNo).getBytes(StandardCharsets.UTF_8)).toString();
        }

        Map<String, Object> properties() {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("title", title);
            p.put("body", body);
            p.put("source", source);
            p.put("chunkNo", chunkNo);
            p.put("lang", lang());
            p.put("kind", DemoDocCollection.KIND_PASSAGE);
            return p;
        }
    }

    private final WeaviateConnector weaviate;

    public SampleDataLoader(WeaviateConnector weaviate) {
        this.weaviate = weaviate;
    }

    /** Drops and re-creates the collection, then inserts every sample passage. Returns the number of passages. */
    public int load() {
        return load(DemoDocCollection.definition());
    }

    /** Tests pass a collection definition with a local vectorizer. */
    int load(CollectionConfig definition) {
        List<Chunk> chunks = readSampleData();
        log.info("📥 loading {} passages into Weaviate (collection dropped and re-created)", chunks.size());
        return weaviate.call("load sample data", client -> {
            if (client.collections.exists(DemoDocCollection.NAME)) client.collections.delete(DemoDocCollection.NAME);
            DemoDocCollection.ensure(client, definition);
            for (int from = 0; from < chunks.size(); from += BATCH) {
                List<WeaviateObject<Map<String, Object>>> objects = new ArrayList<>();
                for (Chunk chunk : chunks.subList(from, Math.min(from + BATCH, chunks.size()))) {
                    objects.add(WeaviateObject.of(o -> o.uuid(chunk.id()).properties(chunk.properties())));
                }
                InsertManyResponse response = client.collections.use(DemoDocCollection.NAME).data.insertMany(objects);
                if (!response.errors().isEmpty()) {
                    throw new IllegalStateException(response.errors().size() + " passages failed: "
                            + response.errors().getFirst());
                }
                log.info("📥   {} / {} passages inserted", Math.min(from + BATCH, chunks.size()), chunks.size());
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

    /** Every {@code sample-data/<lang>/*.md} on the classpath, split into passages, in language then file order. */
    public static List<Chunk> readSampleData() {
        try {
            Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath:sample-data/*/*.md");
            Arrays.sort(files, Comparator.comparing(SampleDataLoader::source));
            List<Chunk> chunks = new ArrayList<>();
            for (Resource file : files) {
                chunks.addAll(split(source(file), file.getContentAsString(StandardCharsets.UTF_8)));
            }
            return chunks;
        } catch (IOException e) {
            throw new IllegalStateException("cannot read sample-data", e);
        }
    }

    /** "vi/02-pcr.md": the language folder and the file name. */
    static String source(Resource file) {
        try {
            String path = file.getURL().getPath();
            String folder = path.substring(0, path.lastIndexOf('/'));
            return folder.substring(folder.lastIndexOf('/') + 1) + "/" + file.getFilename();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
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
