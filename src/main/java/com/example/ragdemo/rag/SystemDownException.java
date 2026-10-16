package com.example.ragdemo.rag;

/**
 * A required service (Weaviate, System 1 or System 2) failed. All three are required in this demo, so the pipeline
 * stops: no fallback, no "carry on without it". The reason never contains a key.
 */
public class SystemDownException extends RuntimeException {

    private final ServiceStatus.Service service;
    private final String reason;

    public SystemDownException(ServiceStatus.Service service, String reason) {
        super(service.label + ": " + reason);
        this.service = service;
        this.reason = reason;
    }

    public ServiceStatus.Service service() {
        return service;
    }

    public String reason() {
        return reason;
    }

    /** As a pill status: 🔴 with the reason. */
    public ServiceStatus toStatus() {
        return ServiceStatus.down(service, reason);
    }
}
