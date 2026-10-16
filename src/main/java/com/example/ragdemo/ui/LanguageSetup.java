package com.example.ragdemo.ui;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Every new visitor starts in English (not the browser's language); the header dropdown changes it. */
@Component
public class LanguageSetup implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> e.getSession().setLocale(Tr.ENGLISH));
        event.getSource().addUIInitListener(e -> e.getUI().setLocale(e.getUI().getSession().getLocale()));
    }
}
