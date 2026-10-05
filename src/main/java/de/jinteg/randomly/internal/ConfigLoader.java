
package de.jinteg.randomly.internal;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

/**
 * Loads configuration properties from system properties and environment variables.
 */
public final class ConfigLoader {

    private static final Instant DEFAULT_RUN_START_TIME = Instant.now();

    /**
     * Constructor.
     */
    public ConfigLoader() {
        // utility class
    }

    /**
     * Loads configuration properties from system properties and environment variables.
     *
     * @return JRandomlyConfig instance with loaded configuration
     */
    public JRandomlyConfig load() {
        JRandomlyVersion.warnIfReplayVersionDiffers();

        Optional<Long> seed = readLong(JRandomlyConfig.PROP_SEED)
                .or(() -> readLongEnv(JRandomlyConfig.ENV_SEED));

        Locale locale = readLocale(JRandomlyConfig.PROP_LOCALE)
                .or(() -> readLocaleEnv(JRandomlyConfig.ENV_LOCALE))
                .orElse(Locale.getDefault());

        double maybeRate = readDouble(JRandomlyConfig.PROP_MAYBE_RATE)
                .or(() -> readDoubleEnv(JRandomlyConfig.ENV_MAYBE_RATE))
                .orElse(JRandomlyConfig.DEFAULT_MAYBE_RATE);

        Instant runStartTime = readInstant(JRandomlyConfig.PROP_RUN_START_TIME)
                .or(() -> readInstantEnv(JRandomlyConfig.ENV_RUN_START_TIME))
                .orElse(DEFAULT_RUN_START_TIME);

        return new JRandomlyConfig(seed, locale, maybeRate, runStartTime);
    }

    private Optional<Instant> readInstant(String sysProp) {
        String v = System.getProperty(sysProp);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(parseInstant(v.trim(), sysProp));
    }

    private Optional<Instant> readInstantEnv(String env) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(parseInstant(v.trim(), env));
    }

    private Instant parseInstant(String text, String source) {
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(text).toInstant();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(
                        "Invalid runStartTime value in " + source
                                + ": expected ISO-8601 Instant (Z) or OffsetDateTime (+/-HH:MM) but was: " + text,
                        e2
                );
            }
        }
    }

    private Optional<Long> readLong(String sysProp) {
        String v = System.getProperty(sysProp);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(Long.parseLong(v.trim()));
    }

    private Optional<Long> readLongEnv(String env) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(Long.parseLong(v.trim()));
    }

    private Optional<Double> readDouble(String sysProp) {
        String v = System.getProperty(sysProp);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(Double.parseDouble(v.trim()));
    }

    private Optional<Double> readDoubleEnv(String env) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(Double.parseDouble(v.trim()));
    }

    private Optional<Locale> readLocale(String sysProp) {
        String v = System.getProperty(sysProp);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(Locale.forLanguageTag(v.trim().replace('_', '-')));
    }

    private Optional<Locale> readLocaleEnv(String env) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(Locale.forLanguageTag(v.trim().replace('_', '-')));
    }
}