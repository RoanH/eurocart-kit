package nl.group9.eurocart.api;

import nl.group9.eurocart.order.Address;

/**
 * @param acceptedTermsVersion version of the T&amp;Cs the customer ticked; must be the current one
 * @param shippingAddress delivery address; required when the cart holds physical products, ignored otherwise
 */
public record CreateOrderRequest(String email, String acceptedTermsVersion, Address shippingAddress) {
}