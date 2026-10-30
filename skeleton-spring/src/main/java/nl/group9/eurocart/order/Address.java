package nl.group9.eurocart.order;

/**
 * Delivery address, only collected when the order contains physical products.
 * The country is always Poland, so it is not part of the address.
 *
 * @param fullName name of the recipient
 * @param street street and house number
 * @param postalCode Polish postal code, {@code NN-NNN}
 * @param city city or town
 */
public record Address(String fullName, String street, String postalCode, String city) {
}
