package com.example.ragdemo.rag;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** The one JDK HttpClient for our plain HTTP calls (health checks, Laya via RestClient). Never follows redirects. */
public final class Http {

    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private Http() {
    }

    /**
     * A Spring {@link RestClient} on the one shared JDK {@link #CLIENT} (one connection pool for the whole app), with a
     * read timeout. Calls block — they run on virtual threads, so many can wait at the same time for free.
     */
    public static RestClient restClient(Duration readTimeout) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(CLIENT);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder().requestFactory(factory).build();
    }

    /** Status and body of an answer — any status, no exception for 4xx / 5xx (the caller decides what they mean). */
    public record Reply(int status, String body) {

        public static Reply of(org.springframework.http.client.ClientHttpResponse response) throws IOException {
            return new Reply(response.getStatusCode().value(),
                    new String(response.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /** GET with an optional bearer key. Throws on network errors and timeouts. */
    public static HttpResponse<String> get(String url, String bearerKey, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(timeout).GET();
        if (bearerKey != null && !bearerKey.isBlank()) request.header("Authorization", "Bearer " + bearerKey);
        return CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** A short, key-free description of a failed call, for a status pill or a trace hop. */
    public static String describe(Throwable e) {
        if (e instanceof java.net.http.HttpTimeoutException) return "timeout";
        if (e instanceof java.net.ConnectException) return "cannot connect";
        if (e instanceof IllegalArgumentException) return "invalid URL";
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null ? "" : ": " + message.lines().findFirst().orElse(""));
    }
}
