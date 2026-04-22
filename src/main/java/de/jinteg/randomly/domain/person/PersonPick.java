package de.jinteg.randomly.domain.person;

import java.time.LocalDate;

/**
 * Person pick with common profile attributes.
 *
 * @param username      unique identifier of the person, e.g. {@code jdoeA12}
 * @param name          given name of the person, e.g. {@code John}
 * @param familyName    family name of the person, e.g. {@code Doe}
 * @param genderCode    gender code of the person, e.g. {@code M} or {@code F}
 * @param birthday      date of birth of the person
 * @param birthPlace    place of birth of the person
 * @param occupation    occupation of the person
 * @param nationality   nationality of the person
 * @param mobileNumber  mobile phone number of the person
 * @param email         email address of the person
 * @param middleName    optional middle name
 * @param preferredName optional preferred name
 * @param languages     language tag list or CSV-like value
 * @param maritalStatus marital status of the person
 *
 *                      <p>Raw format:
 *                      {@code username|name|familyName|genderCode|birthday|birthPlace|occupation|nationality|
 *                      mobileNumber|email|middleName|preferredName|languages|maritalStatus}
 */
public record PersonPick(
    String username,
    String name,
    String familyName,
    String genderCode,
    LocalDate birthday,
    String birthPlace,
    String occupation,
    String nationality,
    String mobileNumber,
    String email,
    String middleName,
    String preferredName,
    String languages,
    String maritalStatus
) {
}