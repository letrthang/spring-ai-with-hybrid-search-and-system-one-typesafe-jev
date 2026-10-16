package com.example.ragdemo.rag;

/**
 * Is one of the three required services usable right now? Shown as a pill (🟢 / 🟡 / 🔴) with a reason.
 * The chat is enabled only when all three are {@link State#READY}. The reason is a {@link Msg} (translated by the UI).
 */
public record ServiceStatus(Service service, State state, Msg reason) {

    public enum Service {
        WEAVIATE, SYSTEM1, SYSTEM2;

        /** i18n key of the service's name, e.g. "service.WEAVIATE" → "🗄️ Weaviate". */
        public String key() {
            return "service." + name();
        }
    }

    public enum State {
        READY("🟢"),
        /** Up but not usable yet: waking up, busy (rate limit), or no data. */
        WAITING("🟡"),
        DOWN("🔴");

        public final String dot;

        State(String dot) {
            this.dot = dot;
        }
    }

    public static ServiceStatus ready(Service service, String key, Object... args) {
        return new ServiceStatus(service, State.READY, Msg.of(key, args));
    }

    public static ServiceStatus waiting(Service service, String key, Object... args) {
        return new ServiceStatus(service, State.WAITING, Msg.of(key, args));
    }

    public static ServiceStatus down(Service service, String key, Object... args) {
        return new ServiceStatus(service, State.DOWN, Msg.of(key, args));
    }

    public boolean isReady() {
        return state == State.READY;
    }
}
