package com.example.ragdemo.rag;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** The one JDK HttpClient for our plain HTTP calls (health checks, Laya). Never follows redirects. */
public final class Http {

    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private Http() {
    }

    /** GET with an optional bearer key. Throws on network errors and timeouts. */
    public static HttpResponse<String> get(String url, String bearerKey, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(timeout).GET();
        if (bearerKey != null && !bearerKey.isBlank()) request.header("Authorization", "Bearer " + bearerKey);
        return CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** A short, key-free description of a failed call, for a status pill or a trace hop. */
    public static String describe(Exception e) {
        if (e instanceof java.net.http.HttpTimeoutException) return "timeout";
        if (e instanceof java.net.ConnectException) return "cannot connect";
        if (e instanceof IllegalArgumentException) return "invalid URL";
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null ? "" : ": " + message.lines().findFirst().orElse(""));
    }
}
