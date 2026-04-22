package de.jinteg.randomly.domain.person;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Registry of gender codes and labels.
 */
final class GenderRegistry {

  private static final String GENDER_CATALOG_PATH = "de/jinteg/randomly/catalog/person/gender";

  private final Map<String, GenderPick> byCode;

  private GenderRegistry(Map<String, GenderPick> byCode) {
    this.byCode = Map.copyOf(byCode);
  }

  /**
   * Loads gender catalog for the given locale.
   *
   * @param locale locale to load catalog for
   * @return gender registry
   */
  public static GenderRegistry load(Locale locale) {
    String resourceName = GENDER_CATALOG_PATH + "_" + locale.getLanguage() + ".properties";
    Properties properties = new Properties();

    try (InputStream in = GenderRegistry.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (in == null) {
        throw new IllegalArgumentException("Gender resource not found: " + resourceName);
      }

      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        properties.load(reader);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load gender properties: " + resourceName, e);
    }

    int count = Integer.parseInt(requireProperty(properties, "gender.count.0", resourceName));
    Map<String, GenderPick> map = new LinkedHashMap<>();

    for (int i = 1; i <= count; i++) {
      String code = requireProperty(properties, "gender.code." + i, resourceName).trim();
      String shortLabel = requireProperty(properties, "gender.short." + i, resourceName).trim();
      String longLabel = requireProperty(properties, "gender.long." + i, resourceName).trim();

      map.put(code, new GenderPick(code, shortLabel, longLabel));
    }

    return new GenderRegistry(map);
  }

  private static String requireProperty(Properties properties, String key, String resourceName) {
    String value = properties.getProperty(key);
    if (value == null) {
      throw new IllegalArgumentException(
          "Missing property '%s' in gender resource %s".formatted(key, resourceName)
      );
    }
    return value;
  }

  /**
   * Returns gender pick for the given code.
   *
   * @param code gender code
   * @return gender pick
   */
  public GenderPick get(String code) {
    return byCode.get(code);
  }

  /**
   * Returns all gender picks.
   *
   * @return gender picks
   */
  public Collection<GenderPick> all() {
    return byCode.values();
  }
}