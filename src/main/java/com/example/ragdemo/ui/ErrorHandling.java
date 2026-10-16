package com.example.ragdemo.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The last safety net: an exception thrown in any click or other UI listener is logged with its stack trace and shown
 * as a red notification ("⚠️ Unexpected error: …") — the page keeps working. Background work has its own net in
 * {@link Background}.
 */
@Component
public class ErrorHandling implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandling.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> e.getSession().setErrorHandler(ErrorHandling::handle));
    }

    static void handle(ErrorEvent event) {
        Throwable e = event.getThrowable();
        if (clientGone(e)) {
            // the browser closed the connection (tab closed, network drop, redeploy): nothing to show, not a bug
            log.debug("client went away: {}", Background.message(e));
            return;
        }
        log.error("UI error: {}", Background.message(e), e);
        UI ui = UI.getCurrent();
        if (ui == null || ui.isClosing()) return;
        Notification.show(Tr.t("common.unexpectedError", Background.message(e)), 6000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.ERROR);
    }

    /** "Broken pipe", "Connection reset", Tomcat's ClientAbortException — anywhere in the cause chain. */
    static boolean clientGone(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String name = t.getClass().getSimpleName();
            String message = t.getMessage() == null ? "" : t.getMessage();
            if (name.equals("ClientAbortException") || name.equals("EofException")
                    || (t instanceof java.io.IOException
                    && (message.contains("Broken pipe") || message.contains("Connection reset")))) {
                return true;
            }
            if (t.getCause() == t) break;
        }
        return false;
    }
}
