package com.example.ragdemo.ui;

import com.example.ragdemo.rag.Msg;
import com.vaadin.flow.component.UI;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Translations: English (default), Vietnamese, Japanese — from {@code src/main/resources/vaadin-i18n/translations*.properties}.
 *
 * <ul>
 *   <li>{@link #t(String, Object...)} — a text by its key, in the language of the current page. Arguments fill
 *       {@code {0}}, {@code {1}}…; an argument written {@code "@some.key"} is itself translated first.</li>
 *   <li>{@link #t(Msg)} — a message the backend produced (a key + arguments), or raw data shown as it is.</li>
 * </ul>
 * No fallback to the server's own language: a key missing in a language falls back to English, then shows "!key".
 */
public final class Tr {

    public static final String BUNDLE = "vaadin-i18n/translations";
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale VIETNAMESE = Locale.of("vi");
    public static final Locale JAPANESE = Locale.JAPANESE;
    public static final List<Locale> LANGUAGES = List.of(ENGLISH, VIETNAMESE, JAPANESE);

    private static final ResourceBundle.Control NO_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private Tr() {
    }

    /** The language of the current page; English outside a page (tests, background without a UI). */
    public static Locale locale() {
        UI ui = UI.getCurrent();
        return ui == null || ui.getLocale() == null ? ENGLISH : ui.getLocale();
    }

    public static String t(String key, Object... args) {
        return t(locale(), key, args);
    }

    public static String t(Msg msg) {
        if (msg == null) return "";
        if (Msg.RAW.equals(msg.key())) return msg.args().isEmpty() ? "" : msg.args().getFirst();
        return t(msg.key(), msg.args().toArray());
    }

    public static String t(Locale locale, String key, Object... args) {
        String pattern = lookup(locale, key);
        if (pattern == null) pattern = lookup(ENGLISH, key);
        if (pattern == null) return "!" + key;
        if (args.length == 0) return pattern;
        Object[] filled = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            filled[i] = args[i] instanceof String s && s.startsWith("@") ? t(locale, s.substring(1)) : args[i];
        }
        return new MessageFormat(pattern, locale).format(filled);
    }

    private static String lookup(Locale locale, String key) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE,
                    LANGUAGES.contains(locale) ? locale : ENGLISH, NO_FALLBACK);
            return bundle.containsKey(key) ? bundle.getString(key) : null;
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
