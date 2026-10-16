package com.example.ragdemo.ui;

import com.example.ragdemo.system2.OpenRouterModels;
import com.example.ragdemo.system2.OpenRouterModels.ModelInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * System 2's models: a searchable dropdown of the OpenRouter models (🆓 free group first, 💲 paid group below; each
 * item = name, id and a details footer) and, under it, the chosen models as an <b>ordered list</b> — System 2 tries
 * them top to bottom, so ↑ ↓ change the fallback order and ✕ removes one.
 */
public class ModelPicker extends Div {

    private final OpenRouterModels source;
    private final ComboBox<ModelInfo> combo = new ComboBox<>("➕ Add a model");
    private final Div chosenList = new Div();
    private final List<String> chosen = new ArrayList<>();
    private final Map<String, ModelInfo> known = new HashMap<>();
    private String firstFree;
    private String firstPaid;

    public ModelPicker(OpenRouterModels source, List<String> initial) {
        this.source = source;
        chosen.addAll(initial);

        combo.setWidthFull();
        combo.setPlaceholder("⏳ Loading OpenRouter models…");
        combo.setItemLabelGenerator(ModelInfo::name);
        combo.setRenderer(new ComponentRenderer<>(this::item));
        combo.getStyle().set("--vaadin-combo-box-overlay-width", "min(520px, 92vw)");
        combo.addValueChangeListener(e -> {
            if (e.getValue() == null) return;
            if (!chosen.contains(e.getValue().id())) chosen.add(e.getValue().id());
            combo.clear();
            showChosen();
        });

        Span label = new Span("Models, tried in order (the first that answers wins)");
        label.addClassNames("text-sm", "font-semibold");
        chosenList.addClassNames("flex", "flex-col", "gap-1");
        add(InfoHint.with(label, Hint.MODELS), chosenList, combo);
        addClassNames("flex", "flex-col", "gap-2", "w-full");
        showChosen();
        addAttachListener(e -> load());
    }

    public List<String> getModels() {
        return List.copyOf(chosen);
    }

    public void setModels(List<String> models) {
        chosen.clear();
        chosen.addAll(models);
        showChosen();
    }

    private void load() {
        Background.run(this, () -> {
            try {
                return source.list();
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }, models -> {
            known.clear();
            models.forEach(m -> known.put(m.id(), m));
            firstFree = models.stream().filter(ModelInfo::free).map(ModelInfo::id).findFirst().orElse(null);
            firstPaid = models.stream().filter(m -> !m.free()).map(ModelInfo::id).findFirst().orElse(null);
            long free = models.stream().filter(ModelInfo::free).count();
            // filter on name and id, so "gemma" or "nvidia/" both work
            combo.setItems((m, filter) -> (m.name() + " " + m.id()).toLowerCase(Locale.ROOT)
                    .contains(filter.toLowerCase(Locale.ROOT)), models);
            combo.setPlaceholder("Search " + models.size() + " models (" + free + " free)…");
            showChosen();
        }, error -> combo.setPlaceholder("⚠️ Model list unavailable: " + error.getMessage()));
    }

    /** One dropdown item: an optional group header, the name, the id, and the details footer. */
    private Div item(ModelInfo m) {
        Div item = new Div();
        item.addClassNames("flex", "flex-col", "py-1", "min-w-0");
        if (m.id().equals(firstFree) || m.id().equals(firstPaid)) {
            Span group = new Span(m.free() ? "🆓 Free models" : "💲 Paid models — need credit on your OpenRouter key");
            group.addClassName("rd-model-group");
            item.add(group);
        }
        Span name = new Span(m.name());
        name.addClassNames("font-semibold", "text-sm", "break-words");
        Span id = new Span(m.id());
        id.addClassNames("rd-muted", "text-xs", "break-all");
        Span details = new Span(m.details());
        details.addClassNames("rd-model-details", "text-xs");
        item.add(name, id, details);
        return item;
    }

    /** The chosen models with ↑ ↓ ✕. */
    private void showChosen() {
        chosenList.removeAll();
        if (chosen.isEmpty()) {
            Span none = new Span("No model chosen — add at least one.");
            none.addClassNames("rd-muted", "text-sm");
            chosenList.add(none);
        }
        for (int i = 0; i < chosen.size(); i++) {
            String id = chosen.get(i);
            ModelInfo info = known.get(id);
            Span text = new Span((i + 1) + ". " + (info == null ? id : info.name()));
            text.addClassNames("text-sm", "font-semibold", "break-words");
            Span sub = new Span(info == null ? id : id + " · " + info.details());
            sub.addClassNames("rd-muted", "text-xs", "break-all");
            Div label = new Div(text, sub);
            label.addClassNames("flex", "flex-col", "min-w-0", "grow");
            int index = i;
            Div buttons = new Div(
                    small("↑", "Move up", index > 0, () -> move(index, -1)),
                    small("↓", "Move down", index < chosen.size() - 1, () -> move(index, 1)),
                    small("✕", "Remove", true, () -> {
                        chosen.remove(index);
                        showChosen();
                    }));
            buttons.addClassNames("flex", "shrink-0");
            Div row = new Div(label, new InfoHint(Hint.MODELS), buttons);
            row.addClassNames("rd-model-row", "flex", "items-center", "gap-2", "px-2", "py-1");
            if (info != null && !info.free()) row.addClassName("rd-model-paid");
            chosenList.add(row);
        }
    }

    private void move(int index, int by) {
        String id = chosen.remove(index);
        chosen.add(index + by, id);
        showChosen();
    }

    private static Button small(String text, String title, boolean enabled, Runnable action) {
        Button b = new Button(text, e -> action.run());
        b.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        b.setEnabled(enabled);
        b.getElement().setAttribute("title", title);
        b.getElement().setAttribute("aria-label", title);
        return b;
    }
}
