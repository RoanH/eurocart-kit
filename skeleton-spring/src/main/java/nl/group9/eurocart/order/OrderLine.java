package nl.group9.eurocart.order;

import java.math.BigDecimal;

/**
 * Snapshot of a cart line at order time.
 *
 * @param lineTotal price times quantity, excluding BTW
 * @param vatRate BTW percentage that was applied
 * @param vat BTW on this line, rounded for display only
 */
public record OrderLine(String productId, String name, int quantity, BigDecimal lineTotal, BigDecimal vatRate,
		BigDecimal vat) {
}
