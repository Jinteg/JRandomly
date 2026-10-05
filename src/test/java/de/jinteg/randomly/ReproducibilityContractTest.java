package de.jinteg.randomly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins generated values for fixed inputs (seed, scope, runStartTime, locale, maybeRate).
 *
 * <p>This test guards the reproducibility contract described in ADR-0008: within the same
 * JRandomly version, the same inputs must produce the same values on every run and JVM.
 *
 * <p>A failure means the output for an existing seed has changed. Do not simply update the
 * expected values. If the change is intended (e.g. catalog data or selection logic changed),
 * update the expected values and document the change in the CHANGELOG under
 * "Reproducibility".
 */
class ReproducibilityContractTest {

  private static final long SEED = 20260101L;

  private static final Instant RUN_START_TIME = Instant.parse("2026-01-01T00:00:00Z");

  private static JRandomly fixed(String scope, Locale locale) {
    return JRandomly.builder()
        .withSeed(SEED)
        .withScope(scope)
        .withRunStartTime(RUN_START_TIME)
        .withLocale(locale)
        .withMaybeRate(0.3)
        .build();
  }

  private static List<String> asStrings(Object... values) {
    return Stream.of(values).map(String::valueOf).toList();
  }

  @Test
  @DisplayName("Core utilities return pinned values")
  void core_values_are_pinned() {
    JRandomly r = fixed("contract#core", Locale.ENGLISH);

    List<String> values = asStrings(
        r.intBetween(1, 1000),
        r.longBetween(1L, 1_000_000_000_000L),
        r.bool(),
        r.doubleBetween(2, 0.0, 100.0),
        Arrays.toString(r.uniqueIndices(3, 10)),
        r.elementOf(List.of("a", "b", "c", "d")),
        r.elementsOf(List.of("a", "b", "c", "d", "e"), 2),
        r.enumOf(DayOfWeek.class)
    );

    assertThat(values).containsExactly(
        "754",
        "414742258120",
        "true",
        "99.01",
        "[7, 0, 1]",
        "b",
        "[e, c]",
        "SATURDAY"
    );
  }

  @Test
  @DisplayName("Text module returns pinned values")
  void text_values_are_pinned() {
    var text = fixed("contract#text", Locale.GERMAN).text();

    List<String> values = asStrings(
        text.alpha(8),
        text.alphaNumeric(10),
        text.numericString(6),
        text.hexString(8),
        text.noun(),
        text.verb(),
        text.adjective(),
        text.compoundName(),
        text.sentence()
    );

    assertThat(values).containsExactly(
        "RPHGcjbO",
        "NV0VauGFuJ",
        "119140",
        "76d839a3",
        "Nebel",
        "sammeln",
        "mächtig",
        "EifrigOzean",
        "Niemand darf der Folter oder grausamer, unmenschlicher oder erniedrigender "
            + "Behandlung oder Strafe unterworfen werden."
    );
  }

  @Test
  @DisplayName("Id module returns pinned values")
  void id_values_are_pinned() {
    var id = fixed("contract#id", Locale.ENGLISH).id();

    List<String> values = asStrings(
        id.uuid(),
        id.longId(),
        id.intIdWithExactDigits(6),
        id.key(10),
        id.prefixedId("ORD-", 12)
    );

    assertThat(values).containsExactly(
        "af1cb3c3-cf2d-43f1-94f4-917f24891893",
        "8730262432943956949",
        "202079",
        "E7j1RAvg1i",
        "ORD-ryRBnRZJ"
    );
  }

  @Test
  @DisplayName("DateTime module returns pinned values anchored to runStartTime")
  void date_time_values_are_pinned() {
    var dateTime = fixed("contract#dateTime", Locale.ENGLISH).dateTime(ZoneOffset.UTC);

    List<String> values = asStrings(
        dateTime.instant(),
        dateTime.instantInPast(1, 30),
        dateTime.localDate(),
        dateTime.localDateBefore(365),
        dateTime.localDateTime(),
        dateTime.localTime()
    );

    assertThat(values).containsExactly(
        "2026-01-01T00:00:00Z",
        "2025-12-18T23:06:18Z",
        "2026-01-01",
        "2025-01-12",
        "2026-01-01T00:00",
        "10:59:34"
    );
  }

  @Test
  @DisplayName("Maybe returns pinned presence pattern")
  void maybe_values_are_pinned() {
    JRandomly r = fixed("contract#maybe", Locale.ENGLISH);

    List<String> values = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      values.add(r.maybeText("v" + i).orElse("<absent>"));
    }

    assertThat(values).containsExactly(
        "v0",
        "<absent>",
        "<absent>",
        "v3",
        "v4",
        "<absent>"
    );
  }

  @Test
  @DisplayName("Finance module returns pinned catalog values")
  void finance_values_are_pinned() {
    var finance = fixed("contract#finance", Locale.ENGLISH).finance();

    List<String> values = asStrings(
        finance.stock(Locale.ENGLISH).symbol(),
        finance.stock(Locale.GERMAN).symbol(),
        finance.stockSymbol(),
        finance.cryptoAsset("EUR").pairSymbol(),
        finance.cryptoPairSymbol("USD")
    );

    assertThat(values).containsExactly(
        "MA",
        "BNR",
        "PANW",
        "XRP-EUR",
        "DOGE-USD"
    );
  }

  @Test
  @DisplayName("Currency values are pinned and independent of the JVM default locale")
  void currency_values_are_pinned() {
    var finance = fixed("contract#currency", Locale.GERMANY).finance();

    List<String> values = asStrings(
        finance.currencyCode(),
        finance.currencyCode(),
        finance.currency(List.of(Currency.getInstance("EUR"))).getCurrencyCode(),
        finance.currencySymbol(),
        finance.currencySymbol(),
        finance.currencyCode(List.of("USD"))
    );

    assertThat(values).containsExactly(
        "SEK",
        "VND",
        "MXN",
        "PHP",
        "R$",
        "AUD"
    );
  }

  @Test
  @DisplayName("Forks return pinned values derived from instance seed and name")
  void fork_values_are_pinned() {
    JRandomly r = fixed("contract#fork", Locale.ENGLISH);

    List<String> values = asStrings(
        r.fork("customer").person().data().username(),
        r.fork("stock").finance().stock().symbol(),
        r.fork("order").fork("items").intBetween(1, 1000),
        r.intBetween(1, 1000)
    );

    assertThat(values).containsExactly(
        "ajohnsonT7",
        "EA",
        "494",
        "722"
    );
  }

  @Test
  @DisplayName("Company module returns pinned catalog values")
  void company_values_are_pinned() {
    var company = fixed("contract#company", Locale.ENGLISH).company();

    List<String> values = asStrings(
        company.data(Locale.ENGLISH).name(),
        company.data(Locale.GERMAN).name(),
        company.data(Locale.JAPANESE).name(),
        company.data(Locale.forLanguageTag("tr")).name()
    );

    assertThat(values).containsExactly(
        "IBM",
        "EnBW",
        "イオン",
        "Koç Holding"
    );
  }

  @Test
  @DisplayName("Person module returns pinned catalog values")
  void person_values_are_pinned() {
    var person = fixed("contract#person", Locale.ENGLISH).person();

    List<String> values = asStrings(
        person.data(Locale.ENGLISH).username(),
        person.data(Locale.GERMAN).username(),
        person.data(Locale.JAPANESE).username(),
        person.data(Locale.forLanguageTag("tr")).username(),
        person.gender(Locale.ENGLISH).code(),
        person.gender(Locale.GERMAN).code()
    );

    assertThat(values).containsExactly(
        "ahughesL9",
        "ckochU2",
        "ykubo19",
        "mustafaC5",
        "U",
        "F"
    );
  }
}
