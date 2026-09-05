package com.dockyard.java11features.dto;

import lombok.Builder;

/**
 * ReadWriteStringResult — java.nio.file.Files.writeString/readString, added
 * in Java 11, replacing the old idiom of manually wrapping streams/readers
 * (or third-party helpers like Apache Commons IO) just to read/write a whole
 * file as a single String.
 */
@Builder
public record ReadWriteStringResult(
        String tempFilePath,
        String contentWritten,
        String contentReadBack,
        boolean roundTripMatched
) { }