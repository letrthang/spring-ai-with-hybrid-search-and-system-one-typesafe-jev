package com.example.ragdemo.ui;

import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.ServiceStatus.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 🧭 How it works — the server side of the {@code <rd-system-map>} Lit web component
 * ({@code src/main/frontend/rd-system-map.ts}): the whole RAG pipeline as one live diagram on the 💬 Chat page.
 *
 * <p>Java sends the translated texts ({@code labels}) and the status of the three services ({@code states},
 * {@code tips}); the component draws the stages, the moving connectors and the status pills in the browser. Its ℹ️
 * buttons fire {@code rd-hint}, and the lesson dialog opens here, like every other ℹ️ of the app.
 */
@Tag("rd-system-map")
@JsModule("./rd-system-map.ts")
public class SystemMap extends Component {

    private final Map<String, String> states = new LinkedHashMap<>();
    private final Map<String, String> tips = new LinkedHashMap<>();

    public SystemMap() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String key : new String[] {"title", "lead", "question", "questionSub", "weaviate", "weaviateSub", "rrf",
                "passages", "passagesSub", "system1", "system1Sub", "system2", "system2Sub", "answer", "answerSub",
                "decision", "online", "waiting", "offline", "checking", "reuse", "reuseSub", "verify",
                "learnedStore", "learnedCount"}) {
            labels.put(key, Tr.t("map." + key));
        }
        labels.put("whatIsThis", Tr.t("common.whatIsThis"));
        getElement().setPropertyMap("labels", labels);
        for (Service s : Service.values()) {
            states.put(s.name(), "UNKNOWN");
            tips.put(s.name(), "");
        }
        push();
        getElement().addEventListener("rd-hint", e -> {
            String name = e.getEventData().get("event.detail.hint").asString("");
            try {
                InfoHint.dialog(Hint.valueOf(name)).open();
            } catch (IllegalArgumentException unknown) {
                // a hint name the server does not know: nothing to open
            }
        }).addEventData("event.detail.hint");
    }

    /** How many answers are learned (shown on the learn branch); -1 = unknown. */
    public void setLearned(long count) {
        getElement().setProperty("learned", count);
    }

    /** A service's status changed: its pill colour, and the connector that leads into it. */
    public void show(ServiceStatus status) {
        states.put(status.service().name(), status.state().name());
        tips.put(status.service().name(), StatusPill.text(status));
        push();
    }

    private void push() {
        getElement().setPropertyMap("states", states);
        getElement().setPropertyMap("tips", tips);
    }
}
