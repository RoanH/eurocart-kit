package nl.group9.eurocart.api;

/**
 * @param acceptedTermsVersion version of the T&amp;Cs the customer ticked; must be the current one
 */
public record CreateOrderRequest(String email, String acceptedTermsVersion) {
}
