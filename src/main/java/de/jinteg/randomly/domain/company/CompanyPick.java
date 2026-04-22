package de.jinteg.randomly.domain.company;

/**
 * Company pick with name, address data, and website.
 *
 * @param name         company name
 * @param street       street name
 * @param streetNumber street number
 * @param city         city name
 * @param zipCode      zip code
 * @param state        state or province
 * @param country      country name
 * @param website      company website
 */
public record CompanyPick(
    String name,
    String street,
    String streetNumber,
    String city,
    String zipCode,
    String state,
    String country,
    String website
) {
}