package de.jinteg.randomly.internal.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NumberedPropertiesCatalog provides a catalog of numbered properties based on locale.
 */
public final class NumberedPropertiesCatalog {

    private static final ConcurrentHashMap<String, List<String>> CACHE = new ConcurrentHashMap<>();

    private NumberedPropertiesCatalog() {
      // utility class
    }

    /**
     * Loads a list of numbered properties from a resource file based on the provided base path and locale.
     *
     * @param basePathWithoutSuffix base path without suffix
     * @param locale                locale to use for catalog selection
     * @return list of numbered properties
     */
    public static List<String> loadList(String basePathWithoutSuffix, Locale locale) {
        Objects.requireNonNull(basePathWithoutSuffix, "basePathWithoutSuffix");
        Objects.requireNonNull(locale, "locale");

        String cacheKey = basePathWithoutSuffix + "|" + locale.toLanguageTag();
        return CACHE.computeIfAbsent(cacheKey, s -> loadListUncached(basePathWithoutSuffix, locale));
    }

    private static List<String> loadListUncached(String basePathWithoutSuffix, Locale locale) {
        List<String> candidateSuffixes = suffixesFor(locale);

        Properties p = new Properties();
        boolean loaded = false;

        for (String suffix : candidateSuffixes) {
            String resource = basePathWithoutSuffix + suffix + ".properties";
            try (InputStream in = NumberedPropertiesCatalog.class.getClassLoader().getResourceAsStream(resource)) {
                if (in != null) {
                    p.clear();
                  p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                    loaded = true;
                    break;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load catalog resource: " + resource, e);
            }
        }

        if (!loaded) {
            throw new IllegalStateException("Catalog not found for basePath=" + basePathWithoutSuffix + " locale=" + locale);
        }

        return toNumberedList(p);
    }

    /**
     * Adds suffixes for the given locale, starting with the most specific (language+country) and falling back to language only.
     * <p>
     * Try most specific first, then fallback to language only.
     * No automatic fallback to _en – if neither language+country nor language match,
     * the caller gets an exception (fail-fast for unsupported locales).
     *
     * @param locale locale to add suffixes for
     * @return list of supported suffixes
     */
    private static List<String> suffixesFor(Locale locale) {
        String lang = locale.getLanguage();
        String country = locale.getCountry();

        List<String> suffixes = new ArrayList<>();
        if (!lang.isBlank() && !country.isBlank()) suffixes.add("_" + lang + "_" + country);
        if (!lang.isBlank()) suffixes.add("_" + lang);

        return suffixes;
    }

    private static List<String> toNumberedList(Properties p) {
        TreeMap<Integer, String> ordered = new TreeMap<>();

        for (String key : p.stringPropertyNames()) {
            int dot = key.lastIndexOf('.');
            if (dot < 0 || dot == key.length() - 1) continue;

            String idxText = key.substring(dot + 1);
            try {
                int idx = Integer.parseInt(idxText);
                ordered.put(idx, p.getProperty(key));
            } catch (NumberFormatException ignored) {
                // ignore non-numbered keys
            }
        }

        if (ordered.isEmpty()) {
            throw new IllegalStateException("Catalog does not contain numbered keys like name.1, name.2, ...");
        }

        return List.copyOf(ordered.values());
    }
}