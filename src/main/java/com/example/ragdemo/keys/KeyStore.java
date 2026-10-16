package com.example.ragdemo.keys;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * The keys and endpoints typed in 🔑 Key Management, saved in the H2 file DB.
 *
 * <p>Every caller (Weaviate, System 1, System 2) asks this class <b>at the moment of each call</b>. Nothing caches a key,
 * so a key saved a second ago is used by the very next call.
 *
 * <p>Only two values are required: the Weaviate URL + key and the OpenRouter key. Everything else has a default.
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
            "nvidia/nemotron-3-super-120b-a12b:free",
            "google/gemma-4-31b-it:free",
            "google/gemma-4-26b-a4b-it:free");
    /** The public Laya Space. With no System 1 key the app calls its keyless route /demo/systemone. */
    public static final String DEFAULT_SYSTEM1_URL = "https://letrthang-aimtas-system-one-laya-slm.hf.space";

    private final ApiSettingRepository repository;
    private final Environment environment;

    public KeyStore(ApiSettingRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
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
        return repository.findById(name).map(ApiSetting::getValue).map(String::strip)
                .orElseGet(() -> configured(name));
    }

    /** The configured default of a setting, e.g. {@code weaviate.url} → {@code ragdemo.defaults.weaviate.url}. */
    String configured(String name) {
        return environment == null ? "" : environment.getProperty("ragdemo.defaults." + name, "").strip();
    }

    /** True when the value comes from the configuration, not from Key Management. */
    public boolean fromConfig(String name) {
        return repository.findById(name).isEmpty() && !configured(name).isBlank();
    }

    /** Saves a value; a blank value removes the setting (so its default applies again). */
    public void set(String name, String value) {
        if (value == null || value.isBlank()) {
            repository.deleteById(name);
        } else {
            repository.save(new ApiSetting(name, value.strip()));
        }
    }

    public boolean has(String name) {
        return !get(name).isBlank();
    }

    /** What the UI may show of a secret: only its last 4 characters, e.g. {@code ••••abcd}. */
    public String masked(String name) {
        return mask(get(name));
    }

    static String mask(String secret) {
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
