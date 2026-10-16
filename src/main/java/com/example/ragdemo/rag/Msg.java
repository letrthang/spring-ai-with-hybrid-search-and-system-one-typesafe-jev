package com.example.ragdemo.rag;

import java.util.Arrays;
import java.util.List;

/**
 * A message the backend produces for people to read — as an i18n <b>key</b> plus its arguments, never as a finished
 * English sentence. The UI turns it into the visitor's language (English, Vietnamese, Japanese) with the
 * {@code vaadin-i18n/translations*.properties} files. Plain data, so it can be stored in the chat history as JSON.
 *
 * <p>{@link #raw(String)} carries text that is data, not UI (a question, a passage title, an answer, a technical error):
 * it is shown as it is.
 */
public record Msg(String key, List<String> args) {

    public static final String RAW = "raw";

    public static Msg of(String key, Object... args) {
        return new Msg(key, Arrays.stream(args).map(String::valueOf).toList());
    }

    public static Msg raw(String text) {
        return of(RAW, text == null ? "" : text);
    }

    /** Readable without a translator (logs, exception messages): the key and its arguments. */
    @Override
    public String toString() {
        return RAW.equals(key) ? args.getFirst() : key + (args.isEmpty() ? "" : " " + args);
    }
}
