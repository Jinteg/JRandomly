package de.jinteg.randomly.domain.company;

/**
 * Company pick with address, contact, and business classification data.
 *
 * @param name         company name
 * @param street       street name
 * @param streetNumber street number
 * @param city         city name
 * @param zipCode      zip code
 * @param state        state or province
 * @param country      country name
 * @param countryCode  ISO-like country code
 * @param website      company website
 * @param email        company email address
 * @param phone        company phone number
 * @param sector       business sector
 * @param industry     business industry
 * @param vatId        VAT or tax identifier
 */
public record CompanyPick(
    String name,
    String street,
    String streetNumber,
    String city,
    String zipCode,
    String state,
    String country,
    String countryCode,
    String website,
    String email,
    String phone,
    String sector,
    String industry,
    String vatId
) {
}