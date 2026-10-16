package com.example.ragdemo.ui;

import com.example.ragdemo.rag.SystemDownException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs slow work (HTTP calls, Weaviate, the RAG pipeline) on a <b>virtual thread</b>, never on the UI thread, then
 * paints the result back with {@code UI.access} — but only if the page is still open.
 *
 * <p>Nothing is lost silently: any failure of the work — also an {@link Error} such as OutOfMemoryError — is logged
 * once with its stack trace and handed to {@code onError}, so the page never waits forever.
 */
public final class Background {

    private static final Logger log = LoggerFactory.getLogger(Background.class);

    private Background() {
    }

    /** As {@link #run(Component, Supplier, Consumer, Consumer)}; a failure shows a red notification. */
    public static <T> void run(Component owner, Supplier<T> work, Consumer<T> paint) {
        run(owner, work, paint, Background::notifyError);
    }

    /** Runs {@code work} in the background, then {@code paint}s its result — or {@code onError} if it failed. */
    public static <T> void run(Component owner, Supplier<T> work, Consumer<T> paint,
                               Consumer<RuntimeException> onError) {
        UI ui = owner.getUI().orElse(null);
        if (ui == null) return;
        Thread.ofVirtual().name("rag-demo-bg").start(() -> {
            Runnable painter;
            try {
                T result = work.get();
                painter = () -> paint.accept(result);
            } catch (Throwable e) {
                RuntimeException failure = asRuntime(e);
                if (!(e instanceof SystemDownException)) {
                    log.error("background work failed: {}", message(e), e);   // a service being down is not a bug
                }
                painter = () -> onError.accept(failure);
            }
            if (ui.isClosing() || !owner.isAttached()) return;   // the user left the page: drop the result
            access(ui, painter);
        });
    }

    /**
     * {@code ui.access} that never throws back at a worker thread: a page closed in the meantime is fine, and a
     * failure while painting is logged and shown instead of killing the worker.
     */
    public static void access(UI ui, Runnable paint) {
        try {
            ui.access(() -> {
                try {
                    paint.run();
                } catch (RuntimeException e) {
                    log.error("painting a result failed: {}", message(e), e);
                    notifyError(e);
                }
            });
        } catch (UIDetachedException closed) {
            // the page was closed while the work ran: nothing to paint
        }
    }

    /** A readable one-line message, also for exceptions without one ("NullPointerException"). */
    public static String message(Throwable e) {
        String m = e.getMessage();
        return m == null || m.isBlank() ? e.getClass().getSimpleName() : m;
    }

    private static void notifyError(RuntimeException e) {
        Notification.show(Tr.t("common.unexpectedError", message(e)), 6000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.ERROR);
    }

    private static RuntimeException asRuntime(Throwable e) {
        return e instanceof RuntimeException r ? r : new IllegalStateException(message(e), e);
    }
}
