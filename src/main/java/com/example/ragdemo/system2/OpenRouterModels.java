package com.example.ragdemo.system2;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.Http;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The models System 2 can use, for the dropdown in 🔑 Key Management.
 *
 * <p>On OpenRouter, {@code GET /models} is public (no key) and lists every model with its name, context length and
 * price. The list is kept for {@value #CACHE_MINUTES} minutes. <b>Free models first</b>, paid ones after — a key with a
 * $0 limit can only use the free ones. Another OpenAI-compatible endpoint returns ids only; they are listed as-is.
 */
@Component
public class OpenRouterModels {

    static final long CACHE_MINUTES = 10;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** One model. Prices are US dollars per 1 million tokens; {@code context} may be null. */
    public record ModelInfo(String id, String name, Integer context, double promptPrice, double completionPrice,
                            boolean tools, boolean free) {

        /** The footer line of a dropdown item, e.g. "🆓 Free · 262K context · 🛠 tools". */
        public String details() {
            String price = free ? "🆓 Free"
                    : String.format(Locale.ROOT, "💲 $%s in / $%s out per 1M tokens", money(promptPrice),
                    money(completionPrice));
            return price + (context == null ? "" : " · " + shortNumber(context) + " context") + (tools ? " · 🛠 tools" : "");
        }
    }

    private final KeyStore keys;
    private List<ModelInfo> cached = List.of();
    private String cachedFor = "";
    private Instant cachedAt = Instant.EPOCH;

    public OpenRouterModels(KeyStore keys) {
        this.keys = keys;
    }

    /** Free models first, then paid; each group by name. Throws if the list cannot be loaded. */
    public synchronized List<ModelInfo> list() throws Exception {
        String base = keys.system2BaseUrl();
        if (base.equals(cachedFor) && cachedAt.plus(Duration.ofMinutes(CACHE_MINUTES)).isAfter(Instant.now())) {
            return cached;
        }
        HttpResponse<String> response = Http.get(base + "/models", keys.system2Key(), Duration.ofSeconds(20));
        if (response.statusCode() != 200) throw new IllegalStateException("model list: HTTP " + response.statusCode());
        cached = parse(response.body());
        cachedFor = base;
        cachedAt = Instant.now();
        return cached;
    }

    static List<ModelInfo> parse(String json) {
        List<ModelInfo> models = new ArrayList<>();
        for (JsonNode m : JSON.readTree(json).path("data")) {
            String id = m.path("id").asString("");
            if (id.isBlank() || !writesText(m)) continue;
            double prompt = perMillion(m.path("pricing").path("prompt"));
            double completion = perMillion(m.path("pricing").path("completion"));
            boolean tools = false;
            for (JsonNode p : m.path("supported_parameters")) tools |= "tools".equals(p.asString(""));
            boolean priced = m.path("pricing").isObject();
            boolean free = id.endsWith(":free") || (priced && prompt == 0 && completion == 0);
            models.add(new ModelInfo(id, m.path("name").asString(id),
                    m.path("context_length").isNumber() ? m.path("context_length").asInt() : null,
                    prompt, completion, tools, free || !priced));
        }
        models.sort(Comparator.comparing((ModelInfo x) -> !x.free()).thenComparing(x -> x.name().toLowerCase(Locale.ROOT)));
        return models;
    }

    /** System 2 must write text: skip image / audio / music models (OpenRouter lists their output modalities). */
    private static boolean writesText(JsonNode m) {
        JsonNode out = m.path("architecture").path("output_modalities");
        if (!out.isArray() || out.isEmpty()) return true;   // not stated (e.g. another endpoint): keep it
        for (JsonNode o : out) if ("text".equals(o.asString(""))) return true;
        return false;
    }

    /** OpenRouter gives the price per token as a string, e.g. "0.0000003" → 0.30 per 1M. */
    private static double perMillion(JsonNode price) {
        try {
            return Double.parseDouble(price.asString("0")) * 1_000_000;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String money(double value) {
        return value >= 10 ? String.format(Locale.ROOT, "%.0f", value) : String.format(Locale.ROOT, "%.2f", value);
    }

    static String shortNumber(int n) {
        if (n >= 1_000_000) return (n % 1_000_000 == 0 ? String.valueOf(n / 1_000_000) : String.format(Locale.ROOT, "%.1f", n / 1e6)) + "M";
        if (n >= 1_000) return Math.round(n / 1024.0) + "K";
        return String.valueOf(n);
    }
}
