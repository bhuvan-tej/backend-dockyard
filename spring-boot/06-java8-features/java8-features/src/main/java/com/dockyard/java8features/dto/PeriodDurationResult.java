package com.dockyard.java8features.dto;

import lombok.Builder;

/**
 * PeriodDurationResult — the two "amount of time" types that replaced manual
 * millisecond math: {@link java.time.Period} measures date-based amounts
 * (years/months/days — calendar-aware, so it handles month-length and leap
 * years correctly), {@link java.time.Duration} measures time-based amounts
 * (hours/minutes/seconds/nanos — a fixed length, ignores calendar rules).
 */
@Builder
public record PeriodDurationResult(
        String periodBetweenDates,
        String durationBetweenTimes,
        long durationInSeconds,
        String note
) { }