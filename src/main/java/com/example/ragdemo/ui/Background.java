package com.example.ragdemo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs slow work (HTTP calls, Weaviate, the RAG pipeline) on a <b>virtual thread</b>, never on the UI thread, then
 * paints the result back with {@code UI.access} — but only if the page is still open.
 */
public final class Background {

    private Background() {
    }

    public static <T> void run(Component owner, Supplier<T> work, Consumer<T> paint) {
        run(owner, work, paint, e -> {
            throw e;
        });
    }

    /** As {@link #run(Component, Supplier, Consumer)}, and a failure of {@code work} is painted by {@code onError}. */
    public static <T> void run(Component owner, Supplier<T> work, Consumer<T> paint,
                               Consumer<RuntimeException> onError) {
        UI ui = owner.getUI().orElse(null);
        if (ui == null) return;
        Thread.ofVirtual().name("rag-demo-bg").start(() -> {
            Runnable painter;
            try {
                T result = work.get();
                painter = () -> paint.accept(result);
            } catch (RuntimeException e) {
                painter = () -> onError.accept(e);
            }
            if (ui.isClosing() || !owner.isAttached()) return;   // the user left the page: drop the result
            ui.access(painter::run);
        });
    }
}
