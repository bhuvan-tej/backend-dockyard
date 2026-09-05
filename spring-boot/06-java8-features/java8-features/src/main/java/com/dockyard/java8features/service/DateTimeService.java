package com.dockyard.java8features.service;

import com.dockyard.java8features.dto.PeriodDurationResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * DateTimeService — the {@code java.time} package (JSR-310), which replaced
 * the old {@code java.util.Date}/{@code Calendar} API. The old types were
 * MUTABLE (every setter changes the object in place — a classic source of
 * shared-state bugs) and NOT thread-safe, months were 0-indexed
 * (January == 0, a legendary source of off-by-one bugs), and {@code Date}
 * conflated "a date," "a time," and "an instant" into a single confusing
 * type.
 *
 * <h2>The new model — one type per concept</h2>
 * <ul>
 *   <li>{@link LocalDate} — a date with no time or zone (a birthday).</li>
 *   <li>{@link LocalTime} — a time with no date or zone (a daily alarm).</li>
 *   <li>{@link LocalDateTime} — both, still no zone (a local wall-clock reading).</li>
 *   <li>{@link ZonedDateTime} — a full date+time+zone (an actual moment as a human reads it, somewhere).</li>
 *   <li>{@link Instant} — a single point on the UTC timeline, zone-agnostic (a machine timestamp).</li>
 * </ul>
 * Every one of these types is IMMUTABLE — every "mutator" method
 * ({@code plusDays}, {@code withYear}, ...) returns a NEW instance and leaves
 * the original untouched, which is what makes them thread-safe by
 * construction.
 *
 * <h2>Period vs Duration</h2>
 * {@link Period} measures a date-based amount (years/months/days) and is
 * calendar-aware — {@code Period.between} correctly accounts for varying
 * month lengths and leap years. {@link Duration} measures a time-based
 * amount (hours/minutes/seconds/nanos) as a fixed length of time, with no
 * calendar awareness at all.
 */
@Service
public class DateTimeService {

    /** LocalDate — a date with no time-of-day or time zone. */
    public String localDateDemo() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusWeeks(1);        // returns a NEW LocalDate — 'today' is untouched
        LocalDate specificDate = LocalDate.of(2024, 2, 29); // 2024 is a leap year — this is a valid date

        return String.format(
                "today=%s, today.plusWeeks(1)=%s (today is unchanged: %s), Feb 29 2024 valid leap day, isLeapYear=%s",
                today, nextWeek, today, specificDate.isLeapYear());
    }

    /** LocalTime — a time-of-day with no date or zone. */
    public String localTimeDemo() {
        LocalTime now = LocalTime.now();
        LocalTime rounded = now.withSecond(0).withNano(0); // strip seconds/nanos — still a NEW instance
        LocalTime noon = LocalTime.NOON;

        return String.format("now=%s, now-with-seconds-stripped=%s, LocalTime.NOON=%s, isBefore(NOON)=%s",
                now, rounded, noon, now.isBefore(noon));
    }

    /** LocalDateTime — combines LocalDate + LocalTime, still no time zone attached. */
    public String localDateTimeDemo() {
        LocalDateTime meeting = LocalDateTime.of(2026, 9, 1, 14, 30);
        LocalDateTime rescheduled = meeting.plusDays(2).minusMinutes(30); // chained, each step returns a new instance

        return String.format("original=%s, plusDays(2).minusMinutes(30)=%s (original unchanged: %s)",
                meeting, rescheduled, meeting);
    }

    /** Period.between(date, date) vs Duration.between(time, time). */
    public PeriodDurationResult periodVsDuration(LocalDate start, LocalDate end, LocalTime startTime, LocalTime endTime) {
        Period period = Period.between(start, end); // date-based: years/months/days, calendar-aware
        Duration duration = Duration.between(startTime, endTime); // time-based: a fixed length, no calendar rules

        return PeriodDurationResult.builder()
                .periodBetweenDates(String.format("%d years, %d months, %d days", period.getYears(), period.getMonths(), period.getDays()))
                .durationBetweenTimes(String.format("%d hours, %d minutes", duration.toHours(), duration.toMinutesPart()))
                .durationInSeconds(duration.getSeconds())
                .note("Period is calendar-aware (handles varying month lengths/leap years); Duration is a fixed length of time with no calendar awareness at all.")
                .build();
    }

    /** DateTimeFormatter — formatting with a custom pattern, instead of the old, not-thread-safe SimpleDateFormat. */
    public String formatterDemo(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern); // immutable and thread-safe — safe to share/reuse across threads
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(formatter);

        return String.format(
                "pattern=\"%s\" -> formatted=\"%s\" (DateTimeFormatter instances ARE thread-safe and reusable — unlike the old, not-thread-safe SimpleDateFormat, a single instance can be shared across every request/thread)",
                pattern, formatted);
    }

    /** ZonedDateTime + Instant, and interop with the legacy java.util.Date via Date.from()/toInstant(). */
    public String zonedAndInstantDemo() {
        Instant now = Instant.now(); // a single point on the UTC timeline — no concept of "zone" at all
        ZonedDateTime tokyoTime = now.atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime nyTime = now.atZone(ZoneId.of("America/New_York"));

        // Legacy interop: java.util.Date can be converted to/from an Instant,
        // which is the recommended bridge when a legacy API forces Date on you.
        Date legacyDate = Date.from(now);
        Instant backToInstant = legacyDate.toInstant();

        return String.format(
                "Instant.now()=%s represents the SAME moment as tokyoTime=%s and nyTime=%s (only the human-readable wall-clock differs). Legacy Date round-trip preserved instant: %s",
                now, tokyoTime, nyTime, now.equals(backToInstant));
    }
}



