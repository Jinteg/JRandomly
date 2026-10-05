package de.jinteg.randomly.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides the JRandomly library version.
 *
 * <p>The version is part of the reproducibility contract (ADR-0008): generated values are
 * only guaranteed to be identical for the same JRandomly version. It is written into
 * {@code jrandomly-version.properties} by Maven resource filtering.
 *
 * <p>This class is internal and not part of the public API.
 */
public final class JRandomlyVersion {

  /**
   * System property naming the JRandomly version a replay was recorded with.
   */
  public static final String PROP_VERSION = "jrandomly.version";

  /**
   * Environment variable naming the JRandomly version a replay was recorded with.
   */
  public static final String ENV_VERSION = "JRANDOMLY_VERSION";

  static final String UNKNOWN = "unknown";

  private static final String RESOURCE = "de/jinteg/randomly/jrandomly-version.properties";

  private static final System.Logger LOG = System.getLogger(JRandomlyVersion.class.getName());

  private static final String CURRENT = load();

  private static final AtomicBoolean MISMATCH_REPORTED = new AtomicBoolean(false);

  private JRandomlyVersion() {
    // utility class
  }

  /**
   * Returns the version of the JRandomly library on the classpath.
   *
   * @return version, e.g. {@code 0.3.0}, or {@code unknown} if it cannot be determined
   */
  public static String current() {
    return CURRENT;
  }

  /**
   * Logs a warning once per JVM if a replay version is configured
   * ({@value #PROP_VERSION} or {@value #ENV_VERSION}) and differs from the current version.
   */
  public static void warnIfReplayVersionDiffers() {
    String replayVersion = System.getProperty(PROP_VERSION);
    if (replayVersion == null || replayVersion.isBlank()) {
      replayVersion = System.getenv(ENV_VERSION);
    }
    if (differsFromCurrent(replayVersion) && MISMATCH_REPORTED.compareAndSet(false, true)) {
      String recorded = replayVersion.trim();
      LOG.log(System.Logger.Level.WARNING,
          () -> "[JRandomly] Replay was recorded with version " + recorded
              + ", but version " + CURRENT + " is running. Generated values may differ"
              + " (see ADR-0008 and the CHANGELOG section 'Reproducibility').");
    }
  }

  /**
   * Returns whether the given replay version differs from the current version.
   *
   * @param replayVersion version a replay was recorded with, may be {@code null}
   * @return {@code true} if both versions are known and differ
   */
  static boolean differsFromCurrent(String replayVersion) {
    return differs(replayVersion, CURRENT);
  }

  static boolean differs(String replayVersion, String currentVersion) {
    Objects.requireNonNull(currentVersion, "currentVersion");
    if (replayVersion == null || replayVersion.isBlank() || UNKNOWN.equals(currentVersion)) {
      return false;
    }
    return !replayVersion.trim().equals(currentVersion);
  }

  static String load() {
    try (InputStream in = JRandomlyVersion.class.getClassLoader().getResourceAsStream(RESOURCE)) {
      if (in == null) {
        return UNKNOWN;
      }
      Properties properties = new Properties();
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        properties.load(reader);
      }
      String version = properties.getProperty("version", "").trim();
      // Unfiltered resource (e.g. built without Maven): placeholder is still present
      return version.isEmpty() || version.contains("${") ? UNKNOWN : version;
    } catch (IOException e) {
      return UNKNOWN;
    }
  }
}
