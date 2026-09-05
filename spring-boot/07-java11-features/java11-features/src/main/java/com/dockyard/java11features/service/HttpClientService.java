package com.dockyard.java11features.service;

import com.dockyard.java11features.dto.HttpClientResult;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * HttpClientService — {@code java.net.http.HttpClient}, standardized in Java
 * 11 (it existed as an incubator module in Java 9/10), replacing the old,
 * famously low-level {@code HttpURLConnection} — no more manually managing
 * connection state, output streams, or response code plumbing.
 *
 * To keep this demo self-contained and independent of external network
 * access (sandboxes, CI, offline dev machines), it starts a tiny embedded
 * {@link HttpServer} on an ephemeral local port at startup and makes every
 * demo request against THAT — the {@code HttpClient} usage itself is 100%
 * real, only the target server is local instead of a public one.
 *
 * <h2>Synchronous vs asynchronous</h2>
 * <ul>
 *   <li>{@code client.send(request, bodyHandler)} — blocks the calling
 *       thread until the full response arrives.</li>
 *   <li>{@code client.sendAsync(request, bodyHandler)} — returns a
 *       {@code CompletableFuture<HttpResponse<T>>} immediately; the calling
 *       thread is never blocked, and you compose further stages
 *       ({@code thenApply}, {@code thenAccept}, ...) exactly like any other
 *       {@code CompletableFuture}.</li>
 * </ul>
 */
@Service
public class HttpClientService {

    private HttpServer embeddedServer;
    private int port;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @PostConstruct
    void startEmbeddedServer() throws IOException {
        embeddedServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        embeddedServer.createContext("/echo", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String body = "Hello from the embedded demo server! query=" + (query == null ? "<none>" : query);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        embeddedServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        embeddedServer.start();
        port = embeddedServer.getAddress().getPort();
    }

    @PreDestroy
    void stopEmbeddedServer() {
        if (embeddedServer != null) {
            embeddedServer.stop(0);
        }
    }

    /** A blocking, synchronous request — HttpClient.send() waits for the full response before returning. */
    public HttpClientResult synchronousRequest(String queryValue) throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:" + port + "/echo?value=" + queryValue);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        long start = System.nanoTime();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); // BLOCKS here
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        return HttpClientResult.builder()
                .requestUri(uri.toString())
                .statusCode(response.statusCode())
                .responseBody(response.body())
                .elapsedMillis(elapsedMillis)
                .build();
    }

    /** A non-blocking, asynchronous request — HttpClient.sendAsync() returns a CompletableFuture immediately. */
    public HttpClientResult asynchronousRequest(String queryValue) throws ExecutionException, InterruptedException {
        URI uri = URI.create("http://localhost:" + port + "/echo?value=" + queryValue);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        long start = System.nanoTime();
        // sendAsync returns IMMEDIATELY — the calling thread is never blocked waiting on the network.
        CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // In a real async pipeline you'd chain .thenApply/.thenAccept here instead of
        // blocking. We .get() ONLY because this method must return a value synchronously
        // to the REST controller that calls it — the ASYNC part already happened above.
        HttpResponse<String> response = future.get();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        return HttpClientResult.builder()
                .requestUri(uri.toString())
                .statusCode(response.statusCode())
                .responseBody(response.body())
                .elapsedMillis(elapsedMillis)
                .build();
    }

}