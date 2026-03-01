
package de.jinteg.randomly;

import de.jinteg.randomly.core.DateTimeRandomly;
import de.jinteg.randomly.core.IdRandomly;
import de.jinteg.randomly.core.TextRandomly;
import de.jinteg.randomly.domain.finance.FinanceRandomly;
import de.jinteg.randomly.internal.*;
import de.jinteg.randomly.maybe.Maybe;
import de.jinteg.randomly.maybe.MaybeContext;
import de.jinteg.randomly.maybe.MaybeString;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

/**
 * Main entry point for the JRandomly library.
 */
public final class JRandomly {

    private static final System.Logger LOG = System.getLogger(JRandomly.class.getName());

    private static final RandomGeneratorFactory<RandomGenerator> RNG_FACTORY = RandomGeneratorFactory.of("L64X128MixRandom");

    private static final AtomicLong RANDOMLY_INSTANCE_COUNTER = new AtomicLong(0);

    /**
     * Auto-generated root seed, used when no external seed is provided.
     * Initialized lazily on first access, cached for the entire JVM lifetime.
     */
    private static final long AUTO_ROOT_SEED = SeedDerivation.seedFromEntropy(System.nanoTime());

    private final JRandomlyConfig config;
    private final RandomGenerator rng;
    private final long instanceSeed;
    private final String scopeLabel;

    private static String initialCaller;

    private JRandomly(JRandomlyConfig config, RandomGenerator rng, long instanceSeed, String scopeLabel) {
        this.config = Objects.requireNonNull(config, "config");
        this.rng = Objects.requireNonNull(rng, "rng");
        this.instanceSeed = instanceSeed;
        this.scopeLabel = scopeLabel;

        LOG.log(System.Logger.Level.DEBUG,
                () -> "[JRandomly] " + scopeLabel + " seed=" + instanceSeed
                        + " runStartTime=" + config.runStartTime()
                        + " locale=" + config.locale().toLanguageTag());

        ReplayFileWriter.writeEntry(scopeLabel, replayInfo(), initialCaller);
    }

    /**
     * Creates a new JRandomly instance with the default configuration.
     * Equivalent to {@code JRandomly.builder().build()}.
     *
     * @return JRandomly instance
     */
    public static JRandomly randomly() {
        initialCaller = captureInitialCaller();
        return builder().build();
    }

    /**
     * Creates a new JRandomly instance with a scopeLabel for parallel-safe determinism.
     * Equivalent to {@code JRandomly.builder().scopeLabel(scopeLabel).build()}.
     *
     * @param scopeLabel scope label for parallel-safe determinism
     * @return JRandomly instance
     */
    public static JRandomly randomly(String scopeLabel) {
        initialCaller = captureInitialCaller();
        return builder().withScope(scopeLabel).build();
    }

    /**
     * Builds a new JRandomly instance.
     *
     * @return JRandomly instance
     */
    public static Builder builder() {
        if (initialCaller == null) {
            initialCaller = captureInitialCaller();
        }
        return new Builder();
    }

    // --- Observability ---

    /**
     * Returns the configured locale for this instance.
     *
     * @return configured locale
     */
    public Locale getLocale() {
        return config.locale();
    }

    /**
     * Returns the configured run start time for this instance.
     *
     * @return configured run start time
     */
    public Instant getRunStartTime() {
        return config.runStartTime();
    }

    /**
     * Returns the configured instance seed for this instance.
     *
     * @return configured instance seed
     */
    public long getInstanceSeed() {
        return instanceSeed;
    }

    /**
     * Returns the configured scope label for this instance.
     *
     * @return configured scope label, or {@code null} if none was provided
     */
    public String getScopeLabel() {
        return scopeLabel;
    }

    /**
     * Returns a string that can be used to reproduce this instance's configuration.
     * Copy-paste friendly for Maven/Gradle CLI arguments.
     * <p>
     * If a root seed was provided, the replay info includes the root seed
     * so the user can reproduce the exact same run.
     *
     * @return replay info string
     */
    public String replayInfo() {
        long effectiveSeed = config.rootSeed().orElse(AUTO_ROOT_SEED);

        return "-Djrandomly.seed=" + effectiveSeed
                + " -Djrandomly.runStartTime=" + config.runStartTime()
                + " -Djrandomly.locale=" + config.locale().toLanguageTag()
                + " -Djrandomly.maybeRate=" + config.maybeRate();
    }

    // --- Core modules ---

    /**
     * Returns a DateTimeRandomly instance for generating date and time values.
     *
     * @return DateTimeRandomly instance
     */
    public DateTimeRandomly dateTime() {
        return new DateTimeRandomly(this, ZoneId.systemDefault());
    }

    /**
     * Returns a DateTimeRandomly instance for generating date and time values in the specified zone.
     *
     * @param zoneId zone ID for date and time generation
     * @return DateTimeRandomly instance
     */
    public DateTimeRandomly dateTime(ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        return new DateTimeRandomly(this, zoneId);
    }

    /**
     * Returns an IdRandomly instance for generating unique identifiers.
     *
     * @return IdRandomly instance
     */
    public IdRandomly id() {
        return new IdRandomly(this);
    }

    /**
     * Returns a TextRandomly instance for generating text values.
     *
     * @return TextRandomly instance
     */
    public TextRandomly text() {
        return new TextRandomly(this);
    }

    // --- Maybe ---

    /**
     * Returns a MaybeContext instance for generating optional values.
     *
     * @return MaybeContext instance
     */
    public MaybeContext maybe() {
        return new MaybeContext(rng, config.maybeRate());
    }

    /**
     * Returns a MaybeContext instance for generating optional values with the specified probability.
     *
     * @param absentProbability probability of generating an absent value
     * @return MaybeContext instance
     */
    public MaybeContext maybe(double absentProbability) {
        return new MaybeContext(rng, absentProbability);
    }

    /**
     * Returns a Maybe instance with the specified value.
     *
     * @param value value to be wrapped in Maybe
     * @param <T>   type of the value
     * @return Maybe instance
     */
    public <T> Maybe<T> maybeOf(T value) {
        return maybe().value(value);
    }

    /**
     * Returns a Maybe instance with the specified nullable value.
     *
     * @param value nullable value to be wrapped in Maybe
     * @param <T>   type of the value
     * @return Maybe instance
     */
    public <T> Maybe<T> maybeOfNullable(T value) {
        return Maybe.ofNullable(value);
    }

    /**
     * Returns a MaybeString instance with the specified value.
     *
     * @param value value to be wrapped in MaybeString
     * @return MaybeString instance
     */
    public MaybeString maybeText(String value) {
        return maybe().text(value);
    }

    // --- Domains ---

    /**
     * Returns a FinanceRandomly instance for generating finance-related values.
     *
     * @return FinanceRandomly instance
     */
    public FinanceRandomly finance() {
        return new FinanceRandomly(this);
    }

    // --- Core utilities ---

    /**
     * Returns a random boolean value.
     *
     * @return random boolean value
     */
    public boolean bool() {
        return rng.nextBoolean();
    }

    /**
     * Returns a random int in the range [lowerInclusive, upperInclusive].
     *
     * @param lowerInclusive lower bound (inclusive)
     * @param upperInclusive upper bound (inclusive)
     * @return random int in the specified range
     */
    public int intBetween(int lowerInclusive, int upperInclusive) {
        if (lowerInclusive > upperInclusive) {
            throw new IllegalArgumentException("lowerInclusive must be <= upperInclusive");
        }
        long boundExclusive = upperInclusive + 1L;
        return (int) rng.nextLong(lowerInclusive, boundExclusive);
    }

    /**
     * Returns a random index in the range [0, boundExclusive]
     *
     * @param boundExclusive exclusive upper bound (index will be in [0, boundExclusive - 1])
     * @return random index in the specified range
     */
    public int index(int boundExclusive) {
        if (boundExclusive <= 0) {
            throw new IllegalArgumentException("boundExclusive must be > 0");
        }
        return rng.nextInt(boundExclusive);
    }

    /**
     * Returns a random long in the range [lowerInclusive, upperInclusive].
     *
     * @param lowerInclusive lower bound (inclusive)
     * @param upperInclusive upper bound (inclusive)
     * @return random long in the specified range
     */
    public long longBetween(long lowerInclusive, long upperInclusive) {
        if (lowerInclusive > upperInclusive) {
            throw new IllegalArgumentException("lowerInclusive must be <= upperInclusive");
        }
        if (lowerInclusive == upperInclusive) {
            return lowerInclusive;
        }
        // For ranges that don't overflow: use nextLong(origin, bound) with an exclusive upper bound.
        // Special handling for MAX_VALUE upper bound to avoid overflow on +1.
        if (upperInclusive < Long.MAX_VALUE) {
            return rng.nextLong(lowerInclusive, upperInclusive + 1);
        }
        // upperInclusive == Long.MAX_VALUE: nextLong(origin, bound) can't represent bound.
        // Rejection-free: draw from [lower, MAX_VALUE] and occasionally include MAX_VALUE.
        long value = rng.nextLong(lowerInclusive, Long.MAX_VALUE);
        // Give MAX_VALUE a fair chance: with probability 1/(range size) we return it.
        if (rng.nextBoolean()) {
            return value;
        }
        return Long.MAX_VALUE;
    }

    /**
     * Returns a random int with exactly {@code digits} decimal digits.
     * <p>
     * Special case: for {@code digits == 1}, values are in the range [0, 9].
     *
     * @param digits number of digits (1 to 10)
     * @return random int with exactly {@code digits} digits
     */
    public int intWithExactDigits(int digits) {
        int lowerInclusive = NumericDigitBounds.intLowerExact(digits, false);
        int upperInclusive = NumericDigitBounds.intUpperExact(digits);
        if (lowerInclusive > upperInclusive) {
            throw new IllegalArgumentException("No int range available for digits=" + digits);
        }
        return intBetween(lowerInclusive, upperInclusive);
    }

    /**
     * Returns a random int with at most {@code maxDigits} decimal digits.
     *
     * @param maxDigits maximum number of digits (1 to 10)
     * @return random int with at most {@code maxDigits} digits
     */
    public int intWithMaxDigits(int maxDigits) {
        int upperInclusive = NumericDigitBounds.intUpperMax(maxDigits);
        return intBetween(0, upperInclusive);
    }

    /**
     * Returns a random long with exactly {@code digits} decimal digits.
     * <p>
     * Special case: for {@code digits == 1}, values are in the range [0, 9].
     *
     * @param digits number of digits (1 to 19)
     * @return random long with exactly {@code digits} digits
     */
    public long longWithExactDigits(int digits) {
        long lowerInclusive = NumericDigitBounds.longLowerExact(digits, false);
        long upperInclusive = NumericDigitBounds.longUpperExact(digits);
        if (lowerInclusive > upperInclusive) {
            throw new IllegalArgumentException("No long range available for digits=" + digits);
        }
        return longBetween(lowerInclusive, upperInclusive);
    }

    /**
     * Returns a random long with at most {@code maxDigits} decimal digits.
     *
     * @param maxDigits maximum number of digits (1 to 19)
     * @return random long with at most {@code maxDigits} digits
     */
    public long longWithMaxDigits(int maxDigits) {
        long upperInclusive = NumericDigitBounds.longUpperMax(maxDigits);
        return longBetween(0L, upperInclusive);
    }

    /**
     * Returns a random double in the range [lowerInclusive, upperExclusive).
     *
     * @param lowerInclusive lower bound (inclusive)
     * @param upperExclusive upper bound (exclusive)
     * @return random double in the specified range
     */
    public double doubleBetween(double lowerInclusive, double upperExclusive) {
        if (lowerInclusive >= upperExclusive) {
            throw new IllegalArgumentException("lowerInclusive must be < upperExclusive");
        }
        return rng.nextDouble(lowerInclusive, upperExclusive);
    }

    /**
     * Returns a random double in the range [lowerInclusive, upperExclusive),
     * rounded to the specified number of decimal places.
     * <p>
     * Example: {@code doubleBetween(2, 0.0, 100.0)} may return {@code 42.37}.
     * <p>
     * Note: {@code double} provides ~15 significant decimal digits (IEEE 754).
     * For higher precision (e.g., 18 decimals in crypto/wei), use {@code BigDecimal} instead.
     *
     * @param decimalPlaces  number of decimal places (0 to 15)
     * @param lowerInclusive lower bound (inclusive)
     * @param upperExclusive upper bound (exclusive)
     * @return random double in the specified range, rounded to the specified decimal places
     */
    public double doubleBetween(int decimalPlaces, double lowerInclusive, double upperExclusive) {
        if (decimalPlaces < 0 || decimalPlaces > 15) {
            throw new IllegalArgumentException("decimalPlaces must be between 0 and 15");
        }
        double raw = doubleBetween(lowerInclusive, upperExclusive);
        double factor = Math.pow(10, decimalPlaces);
        return Math.round(raw * factor) / factor;
    }

    /**
     * Returns {@code count} unique random int values in the range [0, upperBound).
     * Uses a partial Fisher-Yates shuffle for O(count) performance.
     *
     * @param count      number of unique values to generate
     * @param upperBound exclusive upper bound (values will be in [0, upperBound))
     * @return an array of {@code count} unique random int values
     * @throws IllegalArgumentException if count is negative, upperBound is not positive,
     *                                  or count exceeds upperBound
     */
    public int[] uniqueIndices(int count, int upperBound) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        if (upperBound <= 0) {
            throw new IllegalArgumentException("upperBound must be > 0");
        }
        if (count > upperBound) {
            throw new IllegalArgumentException(
                    "count (%d) exceeds upperBound (%d)".formatted(count, upperBound));
        }
        if (count == 0) {
            return new int[0];
        }

        // Partial Fisher-Yates shuffle on an implicit identity permutation
        int[] pool = new int[upperBound];
        for (int i = 0; i < upperBound; i++) {
            pool[i] = i;
        }

        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            int idx = rng.nextInt(upperBound - i) + i;
            // swap pool[i] and pool[idx]
            int tmp = pool[i];
            pool[i] = pool[idx];
            pool[idx] = tmp;
            result[i] = pool[i];
        }
        return result;
    }


    // --- Single element selection ---

    /**
     * Returns a random element from the list.
     *
     * @param <T> type of elements in the list
     * @param list the list to pick a random element from
     * @return random element from the list
     */
    public <T> T elementOf(List<T> list) {
        Objects.requireNonNull(list, "list");
        if (list.isEmpty()) {
            throw new IllegalArgumentException("list must not be empty");
        }
        return list.get(rng.nextInt(list.size()));
    }

    /**
     * Returns a random element from the set.
     *
     * @param <T> type of elements in the set
     * @param set set of elements to choose from
     * @return random element from the set
     */
    public <T> T elementOf(Set<T> set) {
        return elementOf(List.copyOf(set));
    }

    /**
     * Returns a random element from the list, excluding the specified values.
     *
     * @param <T>       type of elements in the list
     * @param list      the list to pick a random element from
     * @param excluding elements to exclude
     * @return random element from the list, excluding the specified values
     * @throws IllegalArgumentException if no eligible elements remain after exclusion
     */
    public <T> T elementOf(List<T> list, Collection<T> excluding) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(excluding, "excluding");
        List<T> filtered = list.stream()
                .filter(e -> !excluding.contains(e))
                .toList();
        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("No elements remain after exclusion");
        }
        return filtered.get(rng.nextInt(filtered.size()));
    }

    /**
     * Returns a random element from the set, excluding the specified values.
     *
     * @param set       set of elements to choose from
     * @param excluding elements to exclude
     * @param <T>       type of elements in the set
     * @return random element from the set, excluding the specified values
     */
    public <T> T elementOf(Set<T> set, Collection<T> excluding) {
        return elementOf(List.copyOf(set), excluding);
    }

    // --- Multiple distinct element selection ---

    /**
     * Returns {@code count} distinct random elements from the list.
     * Selection uses a partial Fisher-Yates shuffle for O(count) performance.
     *
     * @param <T>   type of elements in the list
     * @param list  the list to pick elements from
     * @param count number of distinct elements to return
     * @return list of selected elements
     * @throws IllegalArgumentException if the count exceeds the number of available elements
     */
    public <T> List<T> elementsOf(List<T> list, int count) {
        return elementsOf(list, count, Set.of());
    }

    /**
     * Returns {@code count} distinct random elements from the set.
     * Selection uses a partial Fisher-Yates shuffle for O(count) performance.
     *
     * @param set   set of elements to select from
     * @param count number of distinct elements to return
     * @param <T>   type of elements in the set
     * @return set of selected elements
     * @throws IllegalArgumentException if the count exceeds the number of available elements
     */
    public <T> Set<T> elementsOf(Set<T> set, int count) {
        return new HashSet<>(elementsOf(List.copyOf(set), count));
    }

    /**
     * Returns {@code count} distinct random elements from the list,
     * excluding the specified values.
     *
     * @param <T>       type of elements in the list
     * @param list      the list to pick elements from
     * @param count     number of distinct elements to return
     * @param excluding elements to exclude
     * @return list of selected elements
     * @throws IllegalArgumentException if the count exceeds the number of eligible elements
     */
    public <T> List<T> elementsOf(List<T> list, int count, Collection<T> excluding) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(excluding, "excluding");
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        if (count == 0) {
            return List.of();
        }

        List<T> pool = excluding.isEmpty()
                ? new ArrayList<>(list)
                : list.stream().filter(e -> !excluding.contains(e)).collect(Collectors.toCollection(ArrayList::new));

        if (count > pool.size()) {
            throw new IllegalArgumentException(
                    "count (%d) exceeds eligible pool size (%d)".formatted(count, pool.size()));
        }

        // Partial Fisher-Yates shuffle: select count elements in O(count)
        List<T> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int idx = rng.nextInt(pool.size());
            result.add(pool.get(idx));
            pool.set(idx, pool.getLast());
            pool.removeLast();
        }
        return List.copyOf(result);
    }

    /**
     * Returns {@code count} distinct random elements from the set,
     *
     * @param <T>       type of elements in the set
     * @param set       set of elements to select from
     * @param count     number of distinct elements to return
     * @param excluding elements to exclude
     * @return set of selected elements
     */
    public <T> Set<T> elementsOf(Set<T> set, int count, Collection<T> excluding) {
        return new HashSet<>(elementsOf(List.copyOf(set), count, excluding));
    }

    // --- Enum selection ---

    /**
     * Returns a random enum constant.
     *
     * @param <E>      enum type
     * @param enumType enum class
     * @return random enum constant
     * @throws IllegalArgumentException if the enum has no constants
     */
    public <E extends Enum<E>> E enumOf(Class<E> enumType) {
        Objects.requireNonNull(enumType, "enumType");
        E[] values = enumType.getEnumConstants();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Enum has no constants: " + enumType.getName());
        }
        return values[rng.nextInt(values.length)];
    }

    /**
     * Returns a random enum constant, excluding the specified values.
     *
     * @param enumType  enum class
     *                  Returns a random enum constant, excluding the specified values.
     * @param excluding excluded constants
     * @return random enum constant, excluding the specified values
     * @throws IllegalArgumentException if no eligible constants remain after exclusion
     */
    public <E extends Enum<E>> E enumOf(Class<E> enumType, Collection<E> excluding) {
        return enumsOf(enumType, 1, excluding).getFirst();
    }

    /**
     * Returns {@code count} distinct random enum constants.
     *
     * @param enumType enum class
     * @param count    number of distinct constants to return
     *                 Returns {@code count} distinct random enum constants.
     * @throws IllegalArgumentException if the count exceeds the number of constants
     */
    public <E extends Enum<E>> List<E> enumsOf(Class<E> enumType, int count) {
        return elementsOf(List.of(enumType.getEnumConstants()), count);
    }

    /**
     * Returns {@code count} distinct random enum constants, excluding the specified values.
     *
     * @param enumType  enum class
     * @param count     number of distinct constants to return
     * @param excluding excluded constants
     *                  Returns {@code count} distinct random enum constants, excluding the specified values.
     * @return list of selected enum constants
     */
    public <E extends Enum<E>> List<E> enumsOf(Class<E> enumType, int count, Collection<E> excluding) {
        Objects.requireNonNull(enumType, "enumType");
        return elementsOf(List.of(enumType.getEnumConstants()), count, excluding);
    }

    /**
     * Fluent builder for creating {@link JRandomly} instances.
     */
    public static final class Builder {
        private String scope;
        private Locale locale;
        private Long seed;
        private Instant runStartTime;
        private Double maybeRate;

        private Builder() {
        }

        /**
         * Sets the scope label for the builder instance. This scope label is used to
         * ensure parallel-safe determinism during the creation of a {@code JRandomly} instance.
         *
         * @param scopeLabel the scope label to assign to this builder; must not be null
         * @return the current builder instance with the provided scope label set
         * @throws NullPointerException if {@code scopeLabel} is null
         */
        public Builder withScope(String scopeLabel) {
            this.scope = Objects.requireNonNull(scopeLabel);
            return this;
        }

        /**
         * Sets the locale for the builder instance.
         *
         * @param locale the locale to assign to this builder; must not be null
         * @return the current builder instance with the provided locale set
         * @throws NullPointerException if {@code locale} is null
         */
        public Builder withLocale(Locale locale) {
            this.locale = Objects.requireNonNull(locale);
            return this;
        }

        /**
         * Sets the seed for the builder instance.
         *
         * @param seed the seed to assign to this builder
         * @return the current builder instance with the provided seed set
         */
        public Builder withSeed(long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Sets the run start time for the builder instance.
         *
         * @param runStartTime the run start time to assign to this builder; must not be null
         * @return the current builder instance with the provided run start time set
         * @throws NullPointerException if {@code runStartTime} is null
         */
        public Builder withRunStartTime(Instant runStartTime) {
            this.runStartTime = Objects.requireNonNull(runStartTime);
            return this;
        }

        /**
         * Sets the maybe rate for the builder instance.
         *
         * @param maybeRate the maybe rate to assign to this builder
         * @return the current builder instance with the provided maybe rate set
         */
        public Builder withMaybeRate(double maybeRate) {
            this.maybeRate = maybeRate;
            return this;
        }

        /**
         * Builds a new JRandomly instance.
         *
         * @return JRandomly instance
         */
        public JRandomly build() {
            // 1) Load external config as baseline
            JRandomlyConfig baseCfg = new ConfigLoader().load();

            // 2) Override with builder values (builder wins over external config)
            long effectiveSeed = seed != null ? seed : effectiveRootSeed(baseCfg);
            Locale effectiveLocale = locale != null ? locale : baseCfg.locale();
            Instant effectiveRunStartTime = runStartTime != null ? runStartTime : baseCfg.runStartTime();
            double effectiveMaybeRate = maybeRate != null ? maybeRate : baseCfg.maybeRate();

            JRandomlyConfig cfg = new JRandomlyConfig(
                    seed != null ? Optional.of(seed) : baseCfg.rootSeed(),
                    effectiveLocale,
                    effectiveMaybeRate,
                    effectiveRunStartTime
            );

            // 3) Derive instance seed
            long instanceSeed;
            String scopeLabel;
            if (scope != null) {
                instanceSeed = SeedDerivation.seedForScope(effectiveSeed, scope);
                scopeLabel = "scoped(\"" + scope + "\")";
            } else {
                long idx = RANDOMLY_INSTANCE_COUNTER.getAndIncrement();
                instanceSeed = SeedDerivation.seedForSubstream(effectiveSeed, "randomly#" + idx);
                scopeLabel = "randomly()#" + idx;
            }

            return new JRandomly(cfg, RNG_FACTORY.create(instanceSeed), instanceSeed, scopeLabel);
        }

        /**
         * Returns the effective root seed: external if provided, otherwise the auto-generated one.
         */
        private static long effectiveRootSeed(JRandomlyConfig cfg) {
            return cfg.rootSeed().orElse(AUTO_ROOT_SEED);
        }
    }

    private static String captureInitialCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .filter(f -> !f.getDeclaringClass().equals(JRandomly.class))
                        .findFirst()
                        .map(f -> f.getClassName() + "#" + f.getMethodName())
                        .orElse("unknown"));
    }
}