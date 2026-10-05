package de.jinteg.randomly.internal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Writes replay information to a replay file, by default {@code target/jrandomly-replay.txt}.
 * <p>
 * The file is truncated once per JVM run (on first writing), then appended for all subsequent
 * instance creations. Entries are written under a lock, so lines of parallel tests do not
 * interleave.
 * <p>
 * The location is resolved once per JVM from {@value #PROP_REPLAY_FILE} or
 * {@value #ENV_REPLAY_FILE}: {@code off} (also {@code false}, {@code none}) disables the file,
 * any other value is used as file path. Replay file errors never fail a test run.
 * <p>
 * This class is internal and not part of the public API.
 */
public final class ReplayFileWriter {

    /**
     * System property for the replay file location, or {@code off} to disable it.
     */
    public static final String PROP_REPLAY_FILE = "jrandomly.replayFile";

    /**
     * Environment variable for the replay file location, or {@code off} to disable it.
     */
    public static final String ENV_REPLAY_FILE = "JRANDOMLY_REPLAY_FILE";

    static final Path DEFAULT_REPLAY_FILE = Path.of("target", "jrandomly-replay.txt");

    private static final Set<String> DISABLED_VALUES = Set.of("off", "false", "none");

    private static final System.Logger LOG = System.getLogger(ReplayFileWriter.class.getName());

    // All mutable state is guarded by the class lock (static synchronized methods).
    private static boolean initialized;
    private static boolean explicitlyConfigured;
    private static boolean failureReported;
    private static Path replayFile;

    private ReplayFileWriter() {
    }

    /**
     * Writes a single replay line for the given instance.
     * Called from {@code JRandomly} constructor.
     *
     * @param scopeLabel    the scope label (e.g. {@code scoped("MyTest#x")})
     * @param replayInfo    the CLI-friendly replay string
     * @param initialCaller supplies the caller shown in the file header; only invoked once
     *                      per JVM, when the header is written
     */
    public static void writeEntry(String scopeLabel, String replayInfo,
                                  Supplier<String> initialCaller) {
        String line = Instant.now()
                + " | " + scopeLabel
                + " | " + replayInfo
                + System.lineSeparator();
        try {
            append(line, initialCaller);
        } catch (IOException | UncheckedIOException e) {
            reportFailure(e);
        }
    }

    private static synchronized void append(String line, Supplier<String> initialCaller)
            throws IOException {
        if (!initialized) {
            initialized = true;
            initialize(initialCaller);
        }
        if (replayFile != null) {
            Files.writeString(replayFile, line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        }
    }

    private static void initialize(Supplier<String> initialCaller) throws IOException {
        String configured = configuredValue();
        explicitlyConfigured = configured != null;
        if (configured == null) {
            replayFile = DEFAULT_REPLAY_FILE;
        } else if (DISABLED_VALUES.contains(configured.toLowerCase(Locale.ROOT))) {
            replayFile = null;
            return;
        } else {
            try {
                replayFile = Path.of(configured);
            } catch (InvalidPathException e) {
                replayFile = null;
                throw new IOException("Invalid replay file path '" + configured + "'", e);
            }
        }

        try {
            // Create parent directories if needed (e.g., fresh checkout without target/)
            Path parent = replayFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            // Truncate: write header as first content
            Files.writeString(replayFile, createReplayHeader(initialCaller.get()),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            // Disable further attempts for this JVM run
            replayFile = null;
            throw e;
        }
    }

    private static String configuredValue() {
        String value = System.getProperty(PROP_REPLAY_FILE);
        if (value == null || value.isBlank()) {
            value = System.getenv(ENV_REPLAY_FILE);
        }
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static synchronized void reportFailure(Exception e) {
        // Never fail the test run because of replay file I/O. A path the user configured
        // explicitly gets one visible warning; the default location only logs at DEBUG.
        if (explicitlyConfigured && !failureReported) {
            failureReported = true;
            LOG.log(System.Logger.Level.WARNING,
                    () -> "[JRandomly] Failed to write replay file (" + PROP_REPLAY_FILE + "): "
                            + e.getMessage());
        } else {
            LOG.log(System.Logger.Level.DEBUG,
                    () -> "[JRandomly] Failed to write replay file: " + e.getMessage());
        }
    }

    private static String createReplayHeader(String initialCaller) {
        return "# JRandomly (Version: " + JRandomlyVersion.current() + ") - " + "Replay Info" +
                System.lineSeparator() +
                "# Run started at " + Instant.now() +
                " | System ZoneID: " + ZoneId.systemDefault() +
                " | Java-VM:" + System.getProperty("java.vm.name") +
                " - Version:" + System.getProperty("java.version") +
                " - OS:" + System.getProperty("os.name") +
                System.lineSeparator() +
                "# Initial caller: " + initialCaller +
                System.lineSeparator() +
                "# Paste the -D flags into your Maven/Gradle CLI to reproduce a run." +
                System.lineSeparator() +
                System.lineSeparator();
    }

    /**
     * Resets internal state, so that the next entry resolves the location again and truncates
     * the file. Intended for testing only.
     */
    static synchronized void resetForTesting() {
        initialized = false;
        explicitlyConfigured = false;
        failureReported = false;
        replayFile = null;
    }
}
