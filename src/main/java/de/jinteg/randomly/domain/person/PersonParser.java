package de.jinteg.randomly.domain.person;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Internal parser for raw person catalog lines.
 */
final class PersonParser {

  static final int COLUMN_COUNT = 14;

  private PersonParser() {
    // utility class
  }

  /**
   * Parse a pipe-delimited catalog line into a {@link PersonPick}.
   * <p>
   * Expected format:
   * username|name|familyName|gender|birthday|birthPlace|occupation|nationality|mobileNumber|email|middleName|preferredName|languages|maritalStatus
   */
  static PersonPick parse(String[] parts) {
    if (parts == null || parts.length < COLUMN_COUNT) {
      throw new IllegalArgumentException(
          "Invalid person raw data: " + Arrays.toString(parts)
              + ", expected " + COLUMN_COUNT + " parts separated by '|': "
              + "username|name|familyName|gender|birthday|birthPlace|occupation|nationality|mobileNumber|email|middleName|preferredName|languages|maritalStatus"
      );
    }

    LocalDate birthday = LocalDate.parse(parts[4].trim()); // expects yyyy-MM-dd
    String phone = PhoneValidator.isValid(parts[8].trim()) ? parts[8].trim() : null;
    String email = EmailValidator.isValid(parts[9].trim()) ? parts[9].trim() : null;

    return new PersonPick(
        parts[0].trim(), // username
        parts[1].trim(), // name
        parts[2].trim(), // familyName
        parts[3].trim(), // gender
        birthday,
        parts[5].trim(), // birthPlace
        parts[6].trim(), // occupation
        parts[7].trim(), // nationality
        phone, // mobileNumber
        email,  // email

        // Optional Fields
        getOptional(parts, 10), // middleName
        getOptional(parts, 11), // preferredName
        getOptional(parts, 12), // languages
        getOptional(parts, 13)  // maritalStatus
    );
  }

  private static String trim(String s) {
    return s == null ? null : s.trim();
  }

  private static String getOptional(String[] parts, int index) {
    if (index >= parts.length) return null;
    String v = trim(parts[index]);
    return (v == null || v.isEmpty()) ? null : v;
  }

  public static final class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValidator() {
    }

    public static boolean isValid(String email) {
      return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
  }

  public static final class PhoneValidator {

    // allowed: +1 555 123 4567, +49 151 2345678, 089 123456, 555-1234
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?(\\d[\\s-]?){6,20}$"
    );

    private PhoneValidator() {
    }

    public static boolean isValid(String phone) {
      return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
  }

}
