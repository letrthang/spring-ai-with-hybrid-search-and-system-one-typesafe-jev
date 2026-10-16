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
        log.error("UI error: {}", Background.message(e), e);
        UI ui = UI.getCurrent();
        if (ui == null || ui.isClosing()) return;
        Notification.show(Tr.t("common.unexpectedError", Background.message(e)), 6000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.ERROR);
    }
}
