package de.jinteg.randomly.domain.company;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Internal parser for raw company catalog lines.
 */
final class CompanyParser {

  static final int COLUMN_COUNT = 14;

  private CompanyParser() {
    // utility class
  }

  /**
   * Parses a pipe-delimited catalog line into a {@link CompanyPick}.
   *
   * <p>Expected format:
   * name|street|streetNumber|city|zipCode|state|country|countryCode|website|email|phone|sector|industry|vatId
   *
   * @param parts pipe-delimited raw parts
   * @return parsed company pick
   */
  static CompanyPick parse(String[] parts) {
    if (parts == null || parts.length < COLUMN_COUNT) {
      throw new IllegalArgumentException(
          "Invalid company raw data: " + Arrays.toString(parts)
              + ", expected " + COLUMN_COUNT + " parts separated by '|': "
              + "name|street|streetNumber|city|zipCode|state|country|countryCode|website|email|phone|sector|industry|vatId"
      );
    }

    String email = EmailValidator.isValid(parts[9].trim()) ? parts[9].trim() : null;
    String phone = PhoneValidator.isValid(parts[10].trim()) ? parts[10].trim() : null;

    return new CompanyPick(
        parts[0].trim(),   // name
        parts[1].trim(),   // street
        parts[2].trim(),   // streetNumber
        parts[3].trim(),   // city
        parts[4].trim(),   // zipCode
        parts[5].trim(),   // state
        parts[6].trim(),   // country
        parts[7].trim(),   // countryCode
        parts[8].trim(),   // website
        email,             // email
        phone,             // phone
        parts[11].trim(),  // sector
        parts[12].trim(),  // industry
        parts[13].trim()   // vatId
    );
  }

  static final class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    static boolean isValid(String email) {
      return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
  }

  static final class PhoneValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?(\\d[\\s()-]?){6,20}$"
    );

    private PhoneValidator() {
    }

    static boolean isValid(String phone) {
      return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
  }
}