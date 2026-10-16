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
import com.vaadin.flow.router.HasDynamicTitle;
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
public class KeyManagementView extends Div implements HasDynamicTitle {

    private final KeyStore keys;

    private final OpenRouterModels models;

    public KeyManagementView(KeyStore keys, WeaviateHealth weaviate, SystemTwoHealth systemTwo,
                             SystemOneHealth systemOne, OpenRouterModels models) {
        this.keys = keys;
        this.models = models;
        addClassNames("p-4", "flex", "flex-col", "gap-4", "max-w-screen-xl");

        Paragraph intro = new Paragraph(Tr.t("keys.intro"));
        intro.addClassNames("rd-muted", "m-0");

        Div grid = new Div(weaviateCard(weaviate), systemTwoCard(systemTwo), systemOneCard(systemOne));
        grid.addClassNames("grid", "grid-cols-1", "lg:grid-cols-3", "gap-4");
        add(InfoHint.with(new H2(Tr.t("keys.title")), Hint.KEYS), intro, grid);
    }

    @Override
    public String getPageTitle() {
        return Tr.t("page.keys");
    }

    // ------------------------------------------------------------------ the three cards

    private Component weaviateCard(WeaviateHealth health) {
        TextField url = new TextField(Tr.t("keys.weaviate.url"));
        // the site owner's default cluster is never shown: only a URL typed here is filled in
        url.setValue(keys.typedIn(KeyStore.WEAVIATE_URL) ? keys.get(KeyStore.WEAVIATE_URL) : "");
        url.setPlaceholder(keys.fromConfig(KeyStore.WEAVIATE_URL) ? Tr.t("keys.defaultSet")
                : "xxxx.c0.region.gcp.weaviate.cloud");
        if (keys.fromConfig(KeyStore.WEAVIATE_URL)) url.setHelperText(Tr.t("keys.fromConfigUrl"));
        PasswordField key = secretField(Tr.t("keys.weaviate.key"), KeyStore.WEAVIATE_KEY);
        url.setWidthFull();
        url.setSuffixComponent(new InfoHint(Hint.OWN_KEYS));   // ℹ️ you can use your own cluster instead
        key.setSuffixComponent(new InfoHint(Hint.OWN_KEYS));

        return card(Tr.t("keys.weaviate.title"), Tr.t("keys.weaviate.what"),
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
                }, Tr.t("keys.clear"),
                url, key);
    }

    private Component systemTwoCard(SystemTwoHealth health) {
        PasswordField key = secretField(Tr.t("keys.system2.key"), KeyStore.SYSTEM2_KEY);
        key.setSuffixComponent(new InfoHint(Hint.OWN_KEYS));   // ℹ️ you can use your own OpenRouter key instead
        key.setPlaceholder(keys.has(KeyStore.SYSTEM2_KEY) ? key.getPlaceholder() : "sk-or-v1-…");
        ModelPicker picker = new ModelPicker(models, keys.system2Models());
        TextField baseUrl = new TextField(Tr.t("keys.system2.baseUrl"));
        baseUrl.setValue(keys.get(KeyStore.SYSTEM2_BASE_URL));
        baseUrl.setPlaceholder(KeyStore.DEFAULT_SYSTEM2_BASE_URL);
        baseUrl.setWidthFull();
        Details advanced = new Details(Tr.t("keys.system2.advanced"), baseUrl);

        return card(Tr.t("keys.system2.title"), Tr.t("keys.system2.what"),
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
                }, Tr.t("keys.defaults"),
                key, picker, advanced);
    }

    private Component systemOneCard(SystemOneHealth health) {
        Paragraph none = new Paragraph(Tr.t("keys.system1.none"));
        none.addClassNames("m-0", "text-sm");
        TextField url = new TextField(Tr.t("keys.system1.url"));
        url.setValue(keys.get(KeyStore.SYSTEM1_URL));
        url.setPlaceholder(KeyStore.DEFAULT_SYSTEM1_URL);
        url.setWidthFull();
        PasswordField key = secretField(Tr.t("keys.system1.key"), KeyStore.SYSTEM1_KEY);
        Details advanced = new Details(Tr.t("keys.system1.advanced"), url, key);

        return card(Tr.t("keys.system1.title"), Tr.t("keys.system1.what"),
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
                }, Tr.t("keys.defaults"),
                none, advanced);
    }

    // ------------------------------------------------------------------ one card = title, fields, buttons, status

    private Component card(String title, String what, ServiceStatus.Service service, Supplier<ServiceStatus> check,
                           Runnable save, Runnable reset, String resetLabel, Component... fields) {
        StatusPill status = new StatusPill(service);
        StatusBadge badge = new StatusBadge();   // ● online / starting / offline, top right of the card
        Runnable test = () -> {
            status.showChecking();
            badge.showChecking();
            Background.run(status, check, s -> {
                status.show(s);
                badge.show(s);
            }, e -> {
                ServiceStatus failed = ServiceStatus.down(service, "status.checkFailed", Background.message(e));
                status.show(failed);
                badge.show(failed);
            });
        };

        Button saveButton = new Button(Tr.t("keys.save"), e -> {
            save.run();
            Notification.show(Tr.t("keys.saved", "@" + service.key()));
            test.run();   // saving also tests, so the user sees at once whether the new value works
        });
        saveButton.addThemeVariants(ButtonVariant.PRIMARY);
        Button testButton = new Button(Tr.t("keys.test"), e -> test.run());
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

        Div head = new Div(InfoHint.with(heading, switch (service) {
            case WEAVIATE -> Hint.WEAVIATE;
            case SYSTEM1 -> Hint.SYSTEM1;
            case SYSTEM2 -> Hint.SYSTEM2;
        }), badge);
        head.addClassNames("flex", "items-start", "justify-between", "gap-2");
        Div card = new Div(head, description);
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

    /** Nothing of a key is ever shown — only whether the owner's default or a typed key is in use. */
    private void refreshSecret(PasswordField field, String name) {
        field.clear();
        if (keys.fromConfig(name)) {
            field.setPlaceholder(Tr.t("keys.defaultSet"));
            field.setHelperText(Tr.t("keys.fromConfig"));
        } else if (keys.typedIn(name)) {
            field.setPlaceholder(Tr.t("keys.typedSet"));
            field.setHelperText(Tr.t("keys.savedKeep"));
        } else {
            field.setPlaceholder(Tr.t("keys.notSet"));
            field.setHelperText(null);
        }
    }

    private void saveSecret(PasswordField field, String name) {
        if (!field.getValue().isBlank()) keys.set(name, field.getValue());   // empty = keep the saved key
        refreshSecret(field, name);
    }
}
