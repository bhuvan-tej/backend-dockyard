package com.dockyard.java8features.controller;

import com.dockyard.java8features.dto.Java8DemoResponse;
import com.dockyard.java8features.dto.PeriodDurationResult;
import com.dockyard.java8features.service.DateTimeService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DateTimeController — the {@code java.time} API (JSR-310).
 * See {@link DateTimeService} for the detailed "why".
 */
@RestController
@RequestMapping("/java8/datetime")
@RequiredArgsConstructor
@Validated
public class DateTimeController {

    private final DateTimeService service;

    @GetMapping("/local-date")
    public Java8DemoResponse<String> localDate() {
        return Java8DemoResponse.<String>builder()
                .operation("LocalDate — a date with no time-of-day or time zone")
                .description("Every 'mutator' (plusWeeks, withYear, ...) returns a NEW LocalDate and leaves the original untouched — this immutability is what makes the type thread-safe by construction.")
                .codeSnippet("LocalDate today = LocalDate.now(); LocalDate nextWeek = today.plusWeeks(1); // 'today' is unchanged")
                .result(service.localDateDemo())
                .build();
    }

    @GetMapping("/local-time")
    public Java8DemoResponse<String> localTime() {
        return Java8DemoResponse.<String>builder()
                .operation("LocalTime — a time-of-day with no date or zone")
                .description("Represents something like a daily alarm or a recurring schedule slot — a time that repeats every day, deliberately detached from any specific date.")
                .codeSnippet("LocalTime now = LocalTime.now(); LocalTime rounded = now.withSecond(0).withNano(0);")
                .result(service.localTimeDemo())
                .build();
    }

    @GetMapping("/local-date-time")
    public Java8DemoResponse<String> localDateTime() {
        return Java8DemoResponse.<String>builder()
                .operation("LocalDateTime — LocalDate + LocalTime, still no time zone")
                .description("A 'wall-clock' reading with both a date and a time, but still zone-agnostic — use ZonedDateTime once the zone actually matters (e.g. scheduling across regions).")
                .codeSnippet("LocalDateTime meeting = LocalDateTime.of(2026, 9, 1, 14, 30); meeting.plusDays(2).minusMinutes(30);")
                .result(service.localDateTimeDemo())
                .build();
    }

    @GetMapping("/period-vs-duration")
    public Java8DemoResponse<PeriodDurationResult> periodVsDuration(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "09:00") @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam(defaultValue = "17:30") @DateTimeFormat(pattern = "HH:mm") LocalTime endTime) {
        return Java8DemoResponse.<PeriodDurationResult>builder()
                .operation("Period.between(date, date) vs Duration.between(time, time)")
                .description("Period is date-based and calendar-aware (correctly handles varying month lengths and leap years). Duration is time-based — a fixed length (hours/minutes/seconds), with no calendar awareness at all. Example: /period-vs-duration?start=2026-01-01&end=2026-03-15")
                .codeSnippet("Period.between(startDate, endDate);  Duration.between(startTime, endTime);")
                .result(service.periodVsDuration(start, end, startTime, endTime))
                .build();
    }

    @GetMapping("/formatter")
    public Java8DemoResponse<String> formatter(
            @RequestParam(defaultValue = "yyyy-MM-dd HH:mm:ss") @NotBlank String pattern) {
        return Java8DemoResponse.<String>builder()
                .operation("DateTimeFormatter — custom pattern formatting")
                .description("Replaces the old, NOT-thread-safe SimpleDateFormat (which mutated internal Calendar state per call). DateTimeFormatter is immutable — one instance can safely be shared across every thread/request.")
                .codeSnippet("DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\").format(LocalDateTime.now());")
                .result(service.formatterDemo(pattern))
                .build();
    }

    @GetMapping("/zoned-and-instant")
    public Java8DemoResponse<String> zonedAndInstant() {
        return Java8DemoResponse.<String>builder()
                .operation("ZonedDateTime + Instant, and legacy java.util.Date interop")
                .description("Instant is a zone-agnostic point on the UTC timeline (a machine timestamp). ZonedDateTime attaches a human-readable wall-clock view in a specific zone to that SAME instant. Date.from(instant)/date.toInstant() is the recommended bridge when a legacy API forces java.util.Date on you.")
                .codeSnippet("Instant now = Instant.now(); now.atZone(ZoneId.of(\"Asia/Tokyo\")); Date.from(now); legacyDate.toInstant();")
                .result(service.zonedAndInstantDemo())
                .build();
    }

}