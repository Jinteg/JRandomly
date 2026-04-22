package de.jinteg.randomly.internal.catalog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Quality-check test for catalog property files under {@code de/jinteg/randomly/catalog}.
 *
 * <p>This test validates repository data integrity rather than runtime behavior.
 * It protects catalog files against encoding issues, malformed keys, accidental gaps,
 * duplicate keys, and incomplete numbered record groups.
 *
 * <p>Supported catalog shapes:
 * <ul>
 *   <li>Flat numbered catalogs, e.g. {@code person.en.1=...}</li>
 *   <li>Structured numbered catalogs, e.g. {@code gender.code.1=M}</li>
 *   <li>Additional fields for an existing numbered entry, e.g.
 *       {@code person.en.22.address=...}</li>
 *   <li>Optional metadata entries with id {@code 0}, e.g.
 *       {@code gender.count.0=2}</li>
 * </ul>
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Every catalog file must be valid UTF-8.</li>
 *   <li>Every non-empty, non-comment line must contain a key-value separator.</li>
 *   <li>Keys must contain a numeric id, such as {@code prefix.1} or
 *       {@code prefix.field.1} or {@code prefix.1.suffix}.</li>
 *   <li>Keys must be unique and values must not be empty.</li>
 *   <li>Entries with id {@code 0} are treated as optional metadata only.</li>
 *   <li>Metadata entries do not create a logical business group and are excluded from
 *       numbered-sequence validation.</li>
 *   <li>Each logical business group must contain at least one business entry with
 *       an id greater than {@code 0}.</li>
 *   <li>Within each logical business group, business ids must be unique, gap-free,
 *       and start with {@code 1}.</li>
 *   <li>Additional fields for a business id are allowed, but they must reference an
 *       existing business id of the same group.</li>
 * </ul>
 *
 * <p>Valid examples:
 * <pre>{@code
 * person.en.1=John|Doe|...
 * person.en.2=Jane|Doe|...
 * person.en.2.address=Rose Street|...
 *
 * xde40.1=...
 * xde40.2=...
 * xdem50.1=...
 *
 * gender.count.0=2
 * gender.code.1=M
 * gender.short.1=E
 * gender.long.1=Erkek
 * gender.code.2=F
 * gender.short.2=K
 * gender.long.2=Kadın
 * }</pre>
 *
 * <p>Invalid examples:
 * <pre>{@code
 * person.en.1=...
 * person.en.3=...                  // gap in business ids
 *
 * person.en.22.address=...         // no business entry for id 22
 *
 * person.en.2=
 *
 * person.en.2=...
 * person.en.2=...                  // duplicate key
 * }</pre>
 */
class CatalogIntegrityTest {

  private static final Path CATALOG_ROOT =
      Path.of("src/main/resources/de/jinteg/randomly/catalog");

  private static final Pattern NUMBERED_KEY_PATTERN =
      Pattern.compile("^(?<prefix>.+)\\.(?<number>\\d+)(?<suffix>(?:\\..+)?)$");

  private static final List<String> IGNORE_CATALOG_FILES = List.of(
      "fx_sample_rates.properties"
  );

  private static void assertFileIsValidUtf8(Path propertyFile) throws IOException {
    byte[] bytes = Files.readAllBytes(propertyFile);

    try {
      StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes));
    } catch (CharacterCodingException e) {
      fail("File is not valid UTF-8: " + propertyFile, e);
    }
  }

  private static void assertNumberedCatalogIntegrity(Path propertyFile) throws IOException {
    List<String> lines = Files.readAllLines(propertyFile, StandardCharsets.UTF_8);
    LinkedHashMap<String, EntryInfo> entriesByKey = new LinkedHashMap<>();

    for (int index = 0; index < lines.size(); index++) {
      String originalLine = lines.get(index);
      String line = stripUtf8Bom(originalLine).trim();

      if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
        continue;
      }

      int separatorIndex = findSeparatorIndex(line);
      assertThat(separatorIndex)
          .as("Missing key-value separator in %s at line %s: %s",
              propertyFile, index + 1, originalLine)
          .isGreaterThan(0);

      String key = line.substring(0, separatorIndex).trim();
      String value = line.substring(separatorIndex + 1).trim();

      assertThat(value)
          .as("Property value in %s at line %s must not be empty",
              propertyFile, index + 1)
          .isNotEmpty();

      Matcher matcher = NUMBERED_KEY_PATTERN.matcher(key);
      assertThat(matcher.matches())
          .as("Property key in %s at line %s must contain a numeric id: %s",
              propertyFile, index + 1, key)
          .isTrue();

      assertThat(entriesByKey)
          .as("Duplicate property key in %s at line %s: %s",
              propertyFile, index + 1, key)
          .doesNotContainKey(key);

      String prefix = matcher.group("prefix");
      int number = Integer.parseInt(matcher.group("number"));
      String suffix = matcher.group("suffix");

      entriesByKey.put(key, new EntryInfo(key, value, prefix, number, suffix, index + 1));
    }

    assertThat(entriesByKey)
        .as("Catalog file must contain at least one property entry: %s", propertyFile)
        .isNotEmpty();

    List<EntryInfo> entries = new ArrayList<>(entriesByKey.values());

    List<EntryInfo> businessEntries = entries.stream()
        .filter(entry -> entry.number() > 0)
        .toList();

    assertThat(businessEntries)
        .as("Catalog file must contain at least one business entry with id > 0: %s", propertyFile)
        .isNotEmpty();

    Map<String, List<EntryInfo>> entriesByBusinessGroup = new LinkedHashMap<>();
    for (EntryInfo entry : businessEntries) {
      String businessGroup = determineBusinessGroup(entry);
      entriesByBusinessGroup
          .computeIfAbsent(businessGroup, ignored -> new ArrayList<>())
          .add(entry);
    }

    for (Map.Entry<String, List<EntryInfo>> groupEntry : entriesByBusinessGroup.entrySet()) {
      String businessGroup = groupEntry.getKey();
      List<EntryInfo> groupEntries = groupEntry.getValue();

      List<Integer> businessIds = groupEntries.stream()
          .map(EntryInfo::number)
          .distinct()
          .sorted()
          .toList();

      List<Integer> expectedSequence = java.util.stream.IntStream.rangeClosed(1, businessIds.size())
          .boxed()
          .toList();

      assertThat(businessIds)
          .as("Business ids for group %s in %s must be gap-free and start with 1",
              businessGroup, propertyFile)
          .containsExactlyElementsOf(expectedSequence);

      Set<Integer> baseEntryIds = groupEntries.stream()
          .filter(entry -> entry.suffix().isEmpty())
          .map(EntryInfo::number)
          .collect(java.util.stream.Collectors.toSet());

      for (EntryInfo entry : groupEntries) {
        if (!entry.suffix().isEmpty()) {
          assertThat(baseEntryIds)
              .as("Additional field entry in group %s at line %s in %s requires a base entry without suffix for id %s: %s",
                  businessGroup, entry.lineNumber(), propertyFile, entry.number(), entry.key())
              .contains(entry.number());
        }
      }

      LinkedHashSet<String> variants = new LinkedHashSet<>();
      for (EntryInfo entry : groupEntries) {
        String variantKey = entry.number() + "|" + entry.prefix() + "|" + entry.suffix();
        assertThat(variants.add(variantKey))
            .as("Duplicate numbered variant in business group %s at line %s in %s: %s",
                businessGroup, entry.lineNumber(), propertyFile, entry.key())
            .isTrue();
      }
    }
  }

  /**
   * Determines the logical business group for a business entry.
   *
   * <p>Examples:
   * <ul>
   *   <li>{@code person.en.22} -> {@code person.en}</li>
   *   <li>{@code person.en.22.address} -> {@code person.en}</li>
   *   <li>{@code gender.code.1} -> {@code gender}</li>
   *   <li>{@code gender.short.2} -> {@code gender}</li>
   *   <li>{@code xde40.1} -> {@code xde40}</li>
   * </ul>
   *
   * <p>The rule is:
   * <ul>
   *   <li>For flat catalogs, the prefix itself is the business group.</li>
   *   <li>For structured catalogs, the last prefix segment is treated as a field name
   *       if there are at least three prefix segments. In that case, the business
   *       group is the prefix without the last segment.</li>
   * </ul>
   */
  private static String determineBusinessGroup(EntryInfo entry) {
    String[] segments = entry.prefix().split("\\.");

    if (segments.length >= 3) {
      return String.join(".", java.util.Arrays.copyOf(segments, segments.length - 1));
    }

    return entry.prefix();
  }

  private static int findSeparatorIndex(String line) {
    int equalsIndex = line.indexOf('=');
    int colonIndex = line.indexOf(':');

    if (equalsIndex < 0) {
      return colonIndex;
    }
    if (colonIndex < 0) {
      return equalsIndex;
    }
    return Math.min(equalsIndex, colonIndex);
  }

  private static String stripUtf8Bom(String value) {
    if (!value.isEmpty() && value.charAt(0) == '\uFEFF') {
      return value.substring(1);
    }
    return value;
  }

  @Test
  void all_catalog_properties_files_are_valid_utf8_and_well_formed_numbered_catalogs() throws IOException {
    assertThat(CATALOG_ROOT)
        .exists()
        .isDirectory();

    try (Stream<Path> pathStream = Files.walk(CATALOG_ROOT)) {
      List<Path> propertyFiles = pathStream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".properties"))
          .sorted()
          .toList();

      assertThat(propertyFiles)
          .as("catalog properties files under %s", CATALOG_ROOT)
          .isNotEmpty();

      for (Path propertyFile : propertyFiles) {
        assertFileIsValidUtf8(propertyFile);
        if (!IGNORE_CATALOG_FILES.contains(propertyFile.getFileName().toString())) {
          assertNumberedCatalogIntegrity(propertyFile);
        }
      }
    }
  }

  private record EntryInfo(
      String key,
      String value,
      String prefix,
      int number,
      String suffix,
      int lineNumber
  ) {
  }
}