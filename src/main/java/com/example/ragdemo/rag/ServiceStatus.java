package com.example.ragdemo.rag;

/**
 * Is one of the three required services usable right now? Shown as a pill (🟢 / 🟡 / 🔴) with a reason.
 * The chat is enabled only when all three are {@link State#READY}.
 */
public record ServiceStatus(Service service, State state, String reason) {

    public enum Service {
        WEAVIATE("🗄️ Weaviate"),
        SYSTEM1("⚡ System 1"),
        SYSTEM2("🧠 System 2");

        public final String label;

        Service(String label) {
            this.label = label;
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

    public static ServiceStatus ready(Service service, String reason) {
        return new ServiceStatus(service, State.READY, reason);
    }

    public static ServiceStatus waiting(Service service, String reason) {
        return new ServiceStatus(service, State.WAITING, reason);
    }

    public static ServiceStatus down(Service service, String reason) {
        return new ServiceStatus(service, State.DOWN, reason);
    }

    public boolean isReady() {
        return state == State.READY;
    }

    /** e.g. "🔴 ⚡ System 1: unreachable (timeout)". */
    public String text() {
        return state.dot + " " + service.label + ": " + reason;
    }
}
