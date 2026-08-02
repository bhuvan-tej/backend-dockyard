package com.dockyard.virtualthreads.dto;

import lombok.Builder;

/**
 * ThreadInfo — reports which thread is handling the CURRENT HTTP request.
 * With {@code spring.threads.virtual.enabled: true} (see application.yml),
 * every request is served by its own virtual thread — hit this endpoint
 * concurrently (e.g. with `ab` or a loop of background curls) and you'll see
 * a different virtual thread name per request, with no pool to exhaust.
 */
@Builder
public record ThreadInfo(
        String threadName,
        boolean virtualThread,
        long delayMs
) {
}