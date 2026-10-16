package com.example.ragdemo.rag;

/**
 * A required service (Weaviate, System 1 or System 2) failed. All three are required in this demo, so the pipeline
 * stops: no fallback, no "carry on without it". The reason never contains a key; it is a {@link Msg} the UI translates.
 */
public class SystemDownException extends RuntimeException {

    private final ServiceStatus.Service service;
    private final Msg reason;

    public SystemDownException(ServiceStatus.Service service, String key, Object... args) {
        this(service, Msg.of(key, args));
    }

    public SystemDownException(ServiceStatus.Service service, Msg reason) {
        super(service + ": " + reason);
        this.service = service;
        this.reason = reason;
    }

    public ServiceStatus.Service service() {
        return service;
    }

    public Msg reason() {
        return reason;
    }

    /** As a pill status: 🔴 with the reason. */
    public ServiceStatus toStatus() {
        return new ServiceStatus(service, ServiceStatus.State.DOWN, reason);
    }
}
