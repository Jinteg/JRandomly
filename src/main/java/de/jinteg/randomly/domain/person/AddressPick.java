package de.jinteg.randomly.domain.person;

/**
 * Represents a person's address.
 *
 * @param street       street name
 * @param streetNumber street number
 * @param city         city name
 * @param zipCode      zip code
 * @param state        state or province
 * @param country      country name
 */
public record AddressPick(
    String street,
    String streetNumber,
    String city,
    String zipCode,
    String state,
    String country
) {

}
