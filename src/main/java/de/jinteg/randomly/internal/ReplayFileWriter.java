package de.jinteg.randomly.internal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Writes replay information to {@code target/jrandomly-replay.txt}.
 * <p>
 * The file is truncated once per JVM run (on first writing),
 * then appended for all subsequent instance creations.
 * <p>
 * This class is internal and not part of the public API.
 */
public final class ReplayFileWriter {

  private static final System.Logger LOG = System.getLogger(ReplayFileWriter.class.getName());

  private static final Path REPLAY_FILE = Path.of("target", "jrandomly-replay.txt");

  /**
   * Guards first-write truncation. Once true, all subsequent writes append.
   */
  private static volatile boolean initialized = false;

  private ReplayFileWriter() {
  }

  /**
   * Writes a single replay line for the given instance.
   * Called from {@code JRandomly} constructor.
   *
   * @param scopeLabel the scope label (e.g. {@code scoped("MyTest#x")})
   * @param replayInfo the CLI-friendly replay string
   */
  public static void writeEntry(String scopeLabel, String replayInfo, String initialCaller) {
    try {
      ensureInitialized(initialCaller);

      String line = Instant.now()
          + " | " + scopeLabel
          + " | " + replayInfo
          + System.lineSeparator();

      Files.writeString(REPLAY_FILE, line,
          StandardCharsets.UTF_8,
          StandardOpenOption.APPEND);

    } catch (IOException | UncheckedIOException e) {
      // Never fail the test run because of replay file I/O
      LOG.log(System.Logger.Level.DEBUG,
          () -> "[JRandomly] Failed to write replay file: " + e.getMessage());
    }
  }

  private static synchronized void ensureInitialized(String initialCaller) throws IOException {
    if (!initialized) {
      // Create parent directories if needed (e.g., fresh checkout without target/)
      Path parent = REPLAY_FILE.getParent();
      if (parent != null && Files.notExists(parent)) {
        Files.createDirectories(parent);
      }

      // Truncate: write header as first content
      String header = createReplayHeader(initialCaller);

      Files.writeString(REPLAY_FILE, header,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);

      initialized = true;
    }
  }

  private static String createReplayHeader(String initialCaller) {
    return "# JRandomly (Version: 0.1.0) - " + "Replay Info" +
        System.lineSeparator() +
        "# Run started at " + Instant.now() +
        " | System ZoneID: " + ZoneId.systemDefault() +
        // System.lineSeparator() +
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
   * Resets internal state. Intended for testing only.
   */
  static void resetForTesting() {
    initialized = false;
  }
}
