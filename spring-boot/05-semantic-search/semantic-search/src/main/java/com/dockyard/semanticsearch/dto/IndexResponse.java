package com.dockyard.semanticsearch.dto;

/**
 * Result of an index operation.
 *
 * @param source            the label the texts were stored under
 * @param indexed           how many texts were added by THIS request
 * @param totalIndexed      how many texts are now in the store overall
 * @param message           a human-readable summary
 */
public record IndexResponse(
        String source,
        int indexed,
        int totalIndexed,
        String message
) {}