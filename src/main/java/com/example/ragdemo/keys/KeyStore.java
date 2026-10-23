package com.example.ragdemo.keys;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The keys and endpoints of the three services.
 *
 * <p><b>Where a value comes from</b>, in this order:
 * <ol>
 *   <li>a value typed in 🔑 Key Management — <b>kept in memory only</b> for the connection settings (keys and URLs):
 *       never written to the database, gone after a restart;</li>
 *   <li>the default from the environment ({@code RAGDEMO_WEAVIATE_URL}, {@code RAGDEMO_WEAVIATE_KEY},
 *       {@code RAGDEMO_OPENROUTER_KEY} → {@code ragdemo.defaults.*}), read at runtime;</li>
 *   <li>a built-in default (free models, OpenRouter's URL, the public Laya).</li>
 * </ol>
 * Only the System 2 model list is stored in H2. The owner's default keys and URL are never shown on the page.
 *
 * <p>Every caller (Weaviate, System 1, System 2) asks this class <b>at the moment of each call</b>. Nothing caches a key,
 * so a key saved a second ago is used by the very next call.
 */
@Service
public class KeyStore {

    // ---- setting names (rows in the api_setting table)
    public static final String WEAVIATE_URL = "weaviate.url";
    public static final String WEAVIATE_KEY = "weaviate.key";
    public static final String SYSTEM2_KEY = "system2.key";
    public static final String SYSTEM2_BASE_URL = "system2.baseUrl";
    public static final String SYSTEM2_MODELS = "system2.models";
    public static final String SYSTEM1_URL = "system1.url";
    public static final String SYSTEM1_KEY = "system1.key";

    // ---- defaults: everything except the two keys works out of the box
    public static final String DEFAULT_SYSTEM2_BASE_URL = "https://openrouter.ai/api/v1";
    /**
     * Free OpenRouter models, tried in this order (the first that answers wins). Nemotron first: in the live check
     * (2026-10-16) the free Gemma was busy (429) on every call.
     */
    public static final List<String> DEFAULT_SYSTEM2_MODELS = List.of(
            "nvidia/nemotron-3-ultra-550b-a55b:free",   // first choice; the others take over when it is busy (429)
            "nvidia/nemotron-3-super-120b-a12b:free",
            "google/gemma-4-31b-it:free",
            "google/gemma-4-26b-a4b-it:free");
    /** The public Laya Space. With no System 1 key the app calls its keyless route /demo/systemone. */
    public static final String DEFAULT_SYSTEM1_URL = "https://letrthang-aimtas-system-one-decision-model-laya.hf.space";

    /** Connection settings: what a visitor types is held in memory only — never stored in H2. */
    static final Set<String> IN_MEMORY = Set.of(WEAVIATE_URL, WEAVIATE_KEY, SYSTEM2_KEY, SYSTEM2_BASE_URL,
            SYSTEM1_URL, SYSTEM1_KEY);
    /** The secrets among them: never shown, not even in part. */
    static final Set<String> SECRETS = Set.of(WEAVIATE_KEY, SYSTEM2_KEY, SYSTEM1_KEY);

    private final ApiSettingRepository repository;
    private final Environment environment;
    private final Map<String, String> memory = new ConcurrentHashMap<>();

    public KeyStore(ApiSettingRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    /** Older versions stored keys and URLs in H2: remove them, so no key stays in the database. */
    @PostConstruct
    void forgetStoredConnections() {
        if (repository == null) return;
        for (String name : IN_MEMORY) {
            if (repository.existsById(name)) repository.deleteById(name);
        }
    }

    // ---- typed getters (blank = not set)

    public String weaviateUrl() {
        return stripSlash(get(WEAVIATE_URL));
    }

    public String weaviateKey() {
        return get(WEAVIATE_KEY);
    }

    public String system2Key() {
        return get(SYSTEM2_KEY);
    }

    public String system2BaseUrl() {
        return stripSlash(orDefault(get(SYSTEM2_BASE_URL), DEFAULT_SYSTEM2_BASE_URL));
    }

    /** One model per line in the UI; blank lines ignored. */
    public List<String> system2Models() {
        String saved = get(SYSTEM2_MODELS);
        if (saved.isBlank()) return DEFAULT_SYSTEM2_MODELS;
        List<String> models = Arrays.stream(saved.split("[\\n,]")).map(String::strip).filter(m -> !m.isEmpty()).toList();
        return models.isEmpty() ? DEFAULT_SYSTEM2_MODELS : models;
    }

    public String system1Url() {
        return stripSlash(orDefault(get(SYSTEM1_URL), DEFAULT_SYSTEM1_URL));
    }

    public String system1Key() {
        return get(SYSTEM1_KEY);
    }

    // ---- read / write

    /**
     * The value saved in Key Management; if none, the default from the configuration ({@code ragdemo.defaults.*} in
     * application.yml, filled from environment variables or a git-ignored application-local.yml — see README);
     * else "".
     */
    public String get(String name) {
        String typed = typed(name);
        return typed != null ? typed : configured(name);
    }

    /** What was typed in Key Management (memory for connection settings, H2 for the model list), or null. */
    private String typed(String name) {
        if (IN_MEMORY.contains(name)) return memory.get(name);
        return repository.findById(name).map(ApiSetting::getValue).map(String::strip).orElse(null);
    }

    /** The configured default of a setting, e.g. {@code weaviate.url} → {@code ragdemo.defaults.weaviate.url}. */
    String configured(String name) {
        return environment == null ? "" : environment.getProperty("ragdemo.defaults." + name, "").strip();
    }

    /** True when the value comes from the configuration (the site owner's default), not from Key Management. */
    public boolean fromConfig(String name) {
        return typed(name) == null && !configured(name).isBlank();
    }

    /** Forgets every typed connection setting (tests). */
    void clearTyped() {
        memory.clear();
    }

    /** True when a value typed in Key Management is in use. */
    public boolean typedIn(String name) {
        return typed(name) != null;
    }

    /**
     * Uses a value from now on; a blank value removes it (so the default applies again). Connection settings stay in
     * memory; only the model list is written to H2.
     */
    public void set(String name, String value) {
        boolean blank = value == null || value.isBlank();
        if (IN_MEMORY.contains(name)) {
            if (blank) memory.remove(name);
            else memory.put(name, value.strip());
        } else if (blank) {
            repository.deleteById(name);
        } else {
            repository.save(new ApiSetting(name, value.strip()));
        }
    }

    /**
     * A URL that is safe to show: the site owner's default Weaviate host is masked
     * ({@code https://••••.c0.us-east-1.aws.weaviate.cloud}); a URL typed by a visitor is shown as it is.
     */
    public String shownUrl(String name, String url) {
        if (url == null || url.isBlank() || !fromConfig(name)) return url;
        return url.replaceFirst("^(https?://)[^.]+", "$1••••");
    }

    public boolean has(String name) {
        return !get(name).isBlank();
    }

    /** What the UI may show of a secret: nothing of the key itself. */
    public String masked(String name) {
        return has(name) ? "••••••••" : "";
    }

    public static String mask(String secret) {
        if (secret == null || secret.isBlank()) return "";
        return "••••" + (secret.length() > 8 ? secret.substring(secret.length() - 4) : "");
    }

    private static String orDefault(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }

    private static String stripSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
