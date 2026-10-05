package de.jinteg.randomly.core;

import de.jinteg.randomly.JRandomly;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Provides random date and time values.
 *
 * <p>All values are anchored to {@code runStartTime} instead of the wall clock (ADR-0004):
 * {@link #instant()}, {@link #localDate()} and {@link #localDateTime()} return the anchor
 * itself ("now" / "today" of the test run); the other methods return random values
 * relative to it.
 */
public final class DateTimeRandomly {

    private final JRandomly randomly;
    private final ZoneId zoneId;

    /**
     * Constructor.
     *
     * @param randomly random number generator
     * @param zoneId   time zone
     */
    public DateTimeRandomly(JRandomly randomly, ZoneId zoneId) {
        this.randomly = Objects.requireNonNull(randomly, "randomly must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    }

    // --- Instant ---

    /**
     * Returns the anchor instant: {@code runStartTime} of this instance.
     *
     * <p>This value is not random and not the wall-clock time. It is the test run's "now",
     * identical for all calls of the same run, so that tests can compare generated values
     * against a stable reference. Use {@link #instantBefore(int)},
     * {@link #instantInPast(int, int)} or {@link #instantInFuture(int, int)} for random
     * instants.
     *
     * @return anchor instant ("now" of the test run)
     */
    public Instant instant() {
        return randomly.getRunStartTime();
    }

    /**
     * Returns an instant {@code maxSecondsBack} seconds before the current time.
     *
     * @param maxSecondsBack maximum seconds back from current time
     * @return random instant
     */
    public Instant instantBefore(int maxSecondsBack) {
        if (maxSecondsBack < 1) throw new IllegalArgumentException("maxSecondsBack must be >= 1");
        long offset = randomly.intBetween(1, maxSecondsBack);
        return randomly.getRunStartTime().minusSeconds(offset);
    }

    /**
     * Returns a random instant between {@code from} and {@code to} (both inclusive).
     *
     * @param from start of range
     * @param to   end of range
     * @return random instant
     */
    public Instant instantBetween(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (!from.isBefore(to)) throw new IllegalArgumentException("from must be before to");

        long range = to.getEpochSecond() - from.getEpochSecond();
        long offset = randomly.longBetween(0, range);
        return from.plusSeconds(offset);
    }

    /**
     * Returns a random instant in the past, between {@code minDaysBack} and {@code maxDaysBack}
     * days before the anchor time.
     *
     * @param minDaysBack minimum number of days back
     * @param maxDaysBack maximum number of days back
     * @return random instant
     */
    public Instant instantInPast(int minDaysBack, int maxDaysBack) {
        if (minDaysBack < 0) throw new IllegalArgumentException("minDaysBack must be >= 0");
        if (maxDaysBack < minDaysBack) throw new IllegalArgumentException("maxDaysBack must be >= minDaysBack");

        Instant anchor = randomly.getRunStartTime();
        Instant from = anchor.minus(Duration.ofDays(maxDaysBack));
        Instant to = anchor.minus(Duration.ofDays(minDaysBack));
        return instantBetween(from, to);
    }

    /**
     * Returns a random instant in the future, between {@code minDaysAhead} and {@code maxDaysAhead}
     * days after the anchor time.
     *
     * @param minDaysAhead minimum number of days ahead
     * @param maxDaysAhead maximum number of days ahead
     * @return random instant
     */
    public Instant instantInFuture(int minDaysAhead, int maxDaysAhead) {
        if (minDaysAhead < 0) throw new IllegalArgumentException("minDaysAhead must be >= 0");
        if (maxDaysAhead < minDaysAhead) throw new IllegalArgumentException("maxDaysAhead must be >= minDaysAhead");

        Instant anchor = randomly.getRunStartTime();
        Instant from = anchor.plus(Duration.ofDays(minDaysAhead));
        Instant to = anchor.plus(Duration.ofDays(maxDaysAhead));
        return instantBetween(from, to);
    }

    /**
     * Returns a random local date-time in the past, between {@code minDaysBack} and
     * {@code maxDaysBack} days before the anchor time. Equivalent to the legacy
     * {@code randomDateTime(minDaysBack, maxDaysBack)} pattern.
     *
     * @param minDaysBack minimum number of days back
     * @param maxDaysBack maximum number of days back
     * @return random local date-time
     */
    public LocalDateTime localDateTimeInPast(int minDaysBack, int maxDaysBack) {
        return instantInPast(minDaysBack, maxDaysBack).atZone(zoneId).toLocalDateTime();
    }

    /**
     * Returns a random local date-time in the future, between {@code minDaysAhead} and
     * {@code maxDaysAhead} days after the anchor time.
     *
     * @param minDaysAhead minimum number of days ahead
     * @param maxDaysAhead maximum number of days ahead
     * @return random local date-time
     */
    public LocalDateTime localDateTimeInFuture(int minDaysAhead, int maxDaysAhead) {
        return instantInFuture(minDaysAhead, maxDaysAhead).atZone(zoneId).toLocalDateTime();
    }

    // --- LocalDate ---

    /**
     * Returns the anchor date: the date of {@code runStartTime} in this instance's time zone.
     *
     * <p>This value is not random. It is the test run's "today", identical for all calls of
     * the same run. Use {@link #localDateBefore(int)} or {@link #localDateBetween(LocalDate,
     * LocalDate)} for random dates.
     *
     * @return anchor date ("today" of the test run)
     */
    public LocalDate localDate() {
        return anchorLocalDate();
    }

    /**
     * Returns a random local date before the anchor date minus the specified number of days.
     *
     * @param maxDaysBack maximum number of days back
     * @return random local date
     */
    public LocalDate localDateBefore(int maxDaysBack) {
        if (maxDaysBack < 1) throw new IllegalArgumentException("maxDaysBack must be >= 1");
        int days = randomly.intBetween(1, maxDaysBack);
        return anchorLocalDate().minusDays(days);
    }

    /**
     * Returns a random local date between the specified dates.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return random local date
     */
    public LocalDate localDateBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (!from.isBefore(to)) throw new IllegalArgumentException("from must be before to");

        int range = (int) (to.toEpochDay() - from.toEpochDay());
        int offset = randomly.intBetween(0, range);
        return from.plusDays(offset);
    }

    // --- LocalDateTime ---

    /**
     * Returns the anchor date-time: {@code runStartTime} in this instance's time zone.
     *
     * <p>This value is not random. It is the test run's "now", identical for all calls of
     * the same run. Use {@link #localDateTimeBefore(int)},
     * {@link #localDateTimeInPast(int, int)} or {@link #localDateTimeInFuture(int, int)}
     * for random date-times.
     *
     * @return anchor date-time ("now" of the test run)
     */
    public LocalDateTime localDateTime() {
        return anchorLocalDateTime();
    }

    /**
     * Returns a local date-time before the anchor date-time minus the specified number of days.
     *
     * @param maxDaysBack maximum number of days back
     * @return random local date-time
     */
    public LocalDateTime localDateTimeBefore(int maxDaysBack) {
        if (maxDaysBack < 1) throw new IllegalArgumentException("maxDaysBack must be >= 1");
        int days = randomly.intBetween(1, maxDaysBack);
        int hours = randomly.intBetween(0, 23);
        int minutes = randomly.intBetween(0, 59);
        return anchorLocalDateTime().minusDays(days).minusHours(hours).minusMinutes(minutes);
    }

    // --- LocalTime ---

    /**
     * Returns a random local time.
     *
     * @return random local time
     */
    public LocalTime localTime() {
        int hour = randomly.intBetween(0, 23);
        int minute = randomly.intBetween(0, 59);
        int second = randomly.intBetween(0, 59);
        return LocalTime.of(hour, minute, second);
    }

    /**
     * Returns a date in ISO_LOCAL_DATE_TIME format (yyyy-MM-dd'T'HH:mm:ss)
     *
     * @param localDateTime date-time to format
     * @return formatted date-time string
     */
    public String localDateTimeAsIso(LocalDateTime localDateTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
    }

    /**
     * Returns a date in ISO_LOCAL_DATE format (yyyy-MM-dd)
     *
     * @param localDate date to format
     * @return formatted date string
     */
    public String localDateAsIso(LocalDate localDate) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
    }

    /**
     * Returns a time in ISO_LOCAL_TIME format (HH:mm:ss)
     *
     * @param localTime time to format
     * @return formatted time string
     */
    public String localTimeAsIso(LocalTime localTime) {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
    }

    // --- Helpers ---

    private LocalDate anchorLocalDate() {
        return randomly.getRunStartTime().atZone(zoneId).toLocalDate();
    }

    private LocalDateTime anchorLocalDateTime() {
        return randomly.getRunStartTime().atZone(zoneId).toLocalDateTime();
    }
}