package com.dockyard.java11features.controller;

import com.dockyard.java11features.dto.Java11DemoResponse;
import com.dockyard.java11features.dto.PathOfResult;
import com.dockyard.java11features.dto.ReadWriteStringResult;
import com.dockyard.java11features.service.NioService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * NioController — Path.of and Files.readString/writeString.
 * See {@link NioService} for the detailed "why".
 */
@RestController
@RequestMapping("/java11/nio")
@RequiredArgsConstructor
@Validated
public class NioController {

    private final NioService service;

    @GetMapping("/path-of")
    public Java11DemoResponse<PathOfResult> pathOf() {
        return Java11DemoResponse.<PathOfResult>builder()
                .operation("Path.of(String, String...) / Path.of(URI)")
                .description("A direct replacement for the older Paths.get(...) factory — Path.of lets Path construction stay on the Path type itself instead of routing through a separate Paths class. resolve() combines paths; normalize() collapses '..'/'.' segments with no filesystem access.")
                .codeSnippet("Path.of(\"data\", \"2026\", \"summary.txt\"); base.resolve(\"config/app.yml\"); messy.normalize();")
                .result(service.pathOfDemo())
                .build();
    }

    @GetMapping("/read-write-string")
    public Java11DemoResponse<ReadWriteStringResult> readWriteString(
            @RequestParam(defaultValue = "Hello from Files.writeString!") @NotBlank String content) {
        return Java11DemoResponse.<ReadWriteStringResult>builder()
                .operation("Files.writeString(path, content) / Files.readString(path)")
                .description("Reads/writes an entire file as a single String in one call — replacing manual BufferedReader/BufferedWriter boilerplate or a third-party helper. This demo writes to, then reads from, then deletes a real temporary file.")
                .codeSnippet("Files.writeString(tempFile, content); String readBack = Files.readString(tempFile);")
                .result(service.readWriteStringDemo(content))
                .build();
    }

}