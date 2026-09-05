package com.dockyard.java11features.service;

import com.dockyard.java11features.dto.PathOfResult;
import com.dockyard.java11features.dto.ReadWriteStringResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * NioService — {@code java.nio.file} additions from Java 11 that turn
 * multi-line file-handling idioms into one-liners.
 *
 * <ul>
 *   <li>{@code Path.of(String, String...)} / {@code Path.of(URI)} — a direct
 *       replacement for the older {@code Paths.get(...)} factory method
 *       (which still works and just delegates to {@code Path.of} internally
 *       now — {@code Path.of} was added so {@code Path} construction didn't
 *       need to go through a DIFFERENT class, {@code Paths}, than the type
 *       it produces).</li>
 *   <li>{@code Files.writeString(path, content, options...)} /
 *       {@code Files.readString(path, [charset])} — read/write an entire
 *       file as a single {@code String} in one call, replacing manual
 *       {@code BufferedReader}/{@code BufferedWriter} boilerplate or a
 *       third-party helper like Apache Commons IO's {@code FileUtils}.</li>
 * </ul>
 */
@Service
public class NioService {

    /** Path.of(...) — construction, resolution, and normalization, with no actual file I/O. */
    public PathOfResult pathOfDemo() {
        Path multiSegment = Path.of("data", "2026", "reports", "summary.txt"); // same as Paths.get(...)
        Path fromUri = Path.of(URI.create("file:///tmp/demo.txt"));

        Path base = Path.of("/var/app");
        Path resolved = base.resolve("config/app.yml"); // combines a base path with a relative one

        Path messy = Path.of("/var/app/../app/./config/../data");
        Path normalized = messy.normalize(); // collapses ".." and "." segments without touching the filesystem

        List<String> normalizedSegments = new ArrayList<>();
        normalized.forEach(segment -> normalizedSegments.add(segment.toString()));

        return PathOfResult.builder()
                .pathOfMultipleSegments(multiSegment.toString())
                .pathOfSingleUri(fromUri.toString())
                .resolvedAgainstBase(resolved.toString())
                .normalizedAfterDotDot(normalizedSegments)
                .isAbsolute(base.isAbsolute())
                .build();
    }

    /**
     * Files.writeString/readString — round-trips through a real temp file
     * (created and deleted within this call) so the demo is honest about
     * doing actual file I/O, without leaving anything behind.
     */
    public ReadWriteStringResult readWriteStringDemo(String content) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("java11-features", ".txt");
            Files.writeString(tempFile, content); // one call — no BufferedWriter/FileWriter boilerplate
            String readBack = Files.readString(tempFile); // one call — no BufferedReader loop building a StringBuilder

            return ReadWriteStringResult.builder()
                    .tempFilePath(tempFile.toString())
                    .contentWritten(content)
                    .contentReadBack(readBack)
                    .roundTripMatched(content.equals(readBack))
                    .build();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Failed to demo Files.writeString/readString via a temp file", e);
        } finally {
            // Clean up — this demo shouldn't leave files scattered across /tmp.
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // best-effort cleanup only — not the point of this demo
                }
            }
        }
    }

}