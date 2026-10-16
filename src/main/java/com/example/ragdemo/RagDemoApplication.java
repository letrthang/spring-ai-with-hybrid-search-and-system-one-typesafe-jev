package com.example.ragdemo;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. The app shell: Aura theme, our stylesheet last (so it can override), server push (answers arrive from
 * background threads), and light/dark following the operating system.
 */
@SpringBootApplication
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
@Push
@ColorScheme(ColorScheme.Value.LIGHT_DARK)
public class RagDemoApplication implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "icons/favicon.webp", "128x128");
    }

    public static void main(String[] args) {
        SpringApplication.run(RagDemoApplication.class, args);
    }
}
