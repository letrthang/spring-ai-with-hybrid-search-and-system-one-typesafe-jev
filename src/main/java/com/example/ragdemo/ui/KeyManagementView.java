package com.example.ragdemo.ui;

import com.example.ragdemo.keys.KeyStore;
import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.system1.SystemOneHealth;
import com.example.ragdemo.system2.OpenRouterModels;
import com.example.ragdemo.system2.SystemTwoHealth;
import com.example.ragdemo.weaviate.WeaviateHealth;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.function.Supplier;

/**
 * 🔑 Key Management: the only two keys the demo needs (Weaviate, OpenRouter), plus optional endpoints for people who
 * bring their own System 1 / System 2. Saved in the H2 file DB; every API call reads them from there.
 *
 * <p>A saved key is never sent back to the browser: the field stays empty and shows {@code ••••abcd} as a hint.
 * Leaving a key field empty keeps the saved key.
 */
@Route("keys")
@PageTitle("Key Management · RAG demo")
public class KeyManagementView extends Div {

    private final KeyStore keys;

    private final OpenRouterModels models;

    public KeyManagementView(KeyStore keys, WeaviateHealth weaviate, SystemTwoHealth systemTwo,
                             SystemOneHealth systemOne, OpenRouterModels models) {
        this.keys = keys;
        this.models = models;
        addClassNames("p-4", "flex", "flex-col", "gap-4", "max-w-screen-xl");

        Paragraph intro = new Paragraph("Only two services need a key: Weaviate and OpenRouter. "
                + "Keys are saved in this app's local database and read on every call. On Render the database is "
                + "emptied on every restart, so enter them again after a restart.");
        intro.addClassNames("rd-muted", "m-0");

        Div grid = new Div(weaviateCard(weaviate), systemTwoCard(systemTwo), systemOneCard(systemOne));
        grid.addClassNames("grid", "grid-cols-1", "lg:grid-cols-3", "gap-4");
        add(new H2("🔑 Key Management"), intro, grid);
    }

    // ------------------------------------------------------------------ the three cards

    private Component weaviateCard(WeaviateHealth health) {
        TextField url = new TextField("Cluster URL (REST endpoint)");
        url.setValue(keys.get(KeyStore.WEAVIATE_URL));
        url.setPlaceholder("xxxx.c0.region.gcp.weaviate.cloud");
        PasswordField key = secretField("Admin API key", KeyStore.WEAVIATE_KEY);
        url.setWidthFull();

        return card("🗄️ Weaviate", "Required. Stores the passages; runs BM25 + vector search. "
                        + "Use an Admin key: the app creates its collection and loads sample data.",
                ServiceStatus.Service.WEAVIATE, health::check,
                () -> {
                    keys.set(KeyStore.WEAVIATE_URL, url.getValue());
                    saveSecret(key, KeyStore.WEAVIATE_KEY);
                },
                () -> {
                    keys.set(KeyStore.WEAVIATE_URL, "");
                    keys.set(KeyStore.WEAVIATE_KEY, "");
                    url.clear();
                    refreshSecret(key, KeyStore.WEAVIATE_KEY);
                }, "🗑 Clear",
                url, key);
    }

    private Component systemTwoCard(SystemTwoHealth health) {
        PasswordField key = secretField("OpenRouter API key", KeyStore.SYSTEM2_KEY);
        key.setPlaceholder(keys.has(KeyStore.SYSTEM2_KEY) ? key.getPlaceholder() : "sk-or-v1-…");
        ModelPicker picker = new ModelPicker(models, keys.system2Models());
        TextField baseUrl = new TextField("Base URL (any OpenAI-compatible endpoint)");
        baseUrl.setValue(keys.get(KeyStore.SYSTEM2_BASE_URL));
        baseUrl.setPlaceholder(KeyStore.DEFAULT_SYSTEM2_BASE_URL);
        baseUrl.setWidthFull();
        Details advanced = new Details("Advanced: your own System 2", baseUrl);

        return card("🧠 System 2 — OpenRouter", "Required. The LLM that writes the answer. Free models by default.",
                ServiceStatus.Service.SYSTEM2, health::check,
                () -> {
                    saveSecret(key, KeyStore.SYSTEM2_KEY);
                    boolean defaults = picker.getModels().equals(KeyStore.DEFAULT_SYSTEM2_MODELS)
                            || picker.getModels().isEmpty();
                    keys.set(KeyStore.SYSTEM2_MODELS, defaults ? "" : String.join("\n", picker.getModels()));
                    picker.setModels(keys.system2Models());
                    keys.set(KeyStore.SYSTEM2_BASE_URL, baseUrl.getValue());
                },
                () -> {
                    keys.set(KeyStore.SYSTEM2_MODELS, "");
                    keys.set(KeyStore.SYSTEM2_BASE_URL, "");
                    picker.setModels(keys.system2Models());
                    baseUrl.clear();
                }, "🔄 Defaults",
                key, picker, advanced);
    }

    private Component systemOneCard(SystemOneHealth health) {
        Paragraph none = new Paragraph("Nothing to enter: the app uses the public, keyless Laya route "
                + "(10 calls per minute).");
        none.addClassNames("m-0", "text-sm");
        TextField url = new TextField("Your Laya URL");
        url.setValue(keys.get(KeyStore.SYSTEM1_URL));
        url.setPlaceholder(KeyStore.DEFAULT_SYSTEM1_URL);
        url.setWidthFull();
        PasswordField key = secretField("Its API key (optional)", KeyStore.SYSTEM1_KEY);
        Details advanced = new Details("Advanced: your own System 1", url, key);

        return card("⚡ System 1 — Laya", "Required, no key. The small decision model that keeps or drops each "
                        + "passage before System 2 reads it.",
                ServiceStatus.Service.SYSTEM1, health::check,
                () -> {
                    keys.set(KeyStore.SYSTEM1_URL, url.getValue());
                    saveSecret(key, KeyStore.SYSTEM1_KEY);
                },
                () -> {
                    keys.set(KeyStore.SYSTEM1_URL, "");
                    keys.set(KeyStore.SYSTEM1_KEY, "");
                    url.clear();
                    refreshSecret(key, KeyStore.SYSTEM1_KEY);
                }, "🔄 Defaults",
                none, advanced);
    }

    // ------------------------------------------------------------------ one card = title, fields, buttons, status

    private Component card(String title, String what, ServiceStatus.Service service, Supplier<ServiceStatus> check,
                           Runnable save, Runnable reset, String resetLabel, Component... fields) {
        StatusPill status = new StatusPill(service);
        Runnable test = () -> {
            status.showChecking();
            Background.run(status, check, status::show);
        };

        Button saveButton = new Button("💾 Save", e -> {
            save.run();
            Notification.show(service.label + " saved");
            test.run();   // saving also tests, so the user sees at once whether the new value works
        });
        saveButton.addThemeVariants(ButtonVariant.PRIMARY);
        Button testButton = new Button("🧪 Test", e -> test.run());
        Button resetButton = new Button(resetLabel, e -> {
            reset.run();
            test.run();
        });
        resetButton.addThemeVariants(ButtonVariant.TERTIARY);

        Div buttons = new Div(saveButton, testButton, resetButton);
        buttons.addClassNames("flex", "flex-wrap", "gap-2");

        Paragraph description = new Paragraph(what);
        description.addClassNames("rd-muted", "text-sm", "m-0");
        H3 heading = new H3(title);
        heading.addClassNames("m-0", "text-base");

        Div card = new Div(heading, description);
        card.add(fields);
        card.add(buttons, status);
        card.addClassNames("rd-card", "p-4", "flex", "flex-col", "gap-3");
        card.addAttachListener(e -> test.run());   // show the current state when the page opens
        return card;
    }

    // ------------------------------------------------------------------ secrets never go back to the browser

    private PasswordField secretField(String label, String name) {
        PasswordField field = new PasswordField(label);
        field.setWidthFull();
        field.setRevealButtonVisible(true);
        refreshSecret(field, name);
        return field;
    }

    private void refreshSecret(PasswordField field, String name) {
        field.clear();
        boolean saved = keys.has(name);
        field.setPlaceholder(saved ? keys.masked(name) : "not set");
        field.setHelperText(saved ? "Saved. Leave empty to keep it." : null);
    }

    private void saveSecret(PasswordField field, String name) {
        if (!field.getValue().isBlank()) keys.set(name, field.getValue());   // empty = keep the saved key
        refreshSecret(field, name);
    }
}
