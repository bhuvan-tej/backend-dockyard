package com.dockyard.java11features.controller;

import com.dockyard.java11features.dto.HttpClientResult;
import com.dockyard.java11features.dto.Java11DemoResponse;
import com.dockyard.java11features.service.HttpClientService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * HttpClientController — java.net.http.HttpClient, standardized in Java 11.
 * See {@link HttpClientService} for the detailed "why" (including why these
 * demos hit a tiny embedded local server instead of the public internet).
 */
@RestController
@RequestMapping("/java11/httpclient")
@RequiredArgsConstructor
@Validated
public class HttpClientController {

    private final HttpClientService service;

    @GetMapping("/synchronous")
    public Java11DemoResponse<HttpClientResult> synchronous(
            @RequestParam(defaultValue = "hello") @NotBlank String value) throws IOException, InterruptedException {
        return Java11DemoResponse.<HttpClientResult>builder()
                .operation("HttpClient.send(request, bodyHandler) — blocking, synchronous")
                .description("Blocks the calling thread until the FULL response has arrived. Standardized in Java 11 (it was an incubator module in 9/10), replacing the old, low-level HttpURLConnection. This demo targets a tiny embedded local server, not the public internet, so it never depends on external network access.")
                .codeSnippet("HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());")
                .result(service.synchronousRequest(value))
                .build();
    }

    @GetMapping("/asynchronous")
    public Java11DemoResponse<HttpClientResult> asynchronous(
            @RequestParam(defaultValue = "hello") @NotBlank String value) throws ExecutionException, InterruptedException {
        return Java11DemoResponse.<HttpClientResult>builder()
                .operation("HttpClient.sendAsync(request, bodyHandler) — non-blocking, asynchronous")
                .description("Returns a CompletableFuture<HttpResponse<T>> immediately — the calling thread is never blocked waiting on the network. A real async pipeline would chain thenApply/thenAccept instead of calling .get(); this demo calls .get() only because a REST controller must return a value synchronously.")
                .codeSnippet("CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());")
                .result(service.asynchronousRequest(value))
                .build();
    }

}