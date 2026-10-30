package nl.group9.eurocart.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;

/**
 * @param lineTotal price times quantity, excluding BTW, rounded for display only
 * @param vatRate BTW percentage that applies to this line
 * @param vat BTW on this line, rounded for display only; the cart BTW is computed from unrounded amounts
 */
public record CartLine(Product product, int quantity, BigDecimal lineTotal, BigDecimal vatRate, BigDecimal vat) {

	/**
	 * @param exactNet unrounded line amount excluding BTW, from the exact local price times the quantity
	 */
	public static CartLine of(Product product, int quantity, BigDecimal exactNet) {
		BigDecimal vatRate = VatRates.rate(Country.PL, product.category());
		return new CartLine(product, quantity, exactNet.setScale(2, RoundingMode.HALF_UP), vatRate,
				exactVat(exactNet, vatRate).setScale(2, RoundingMode.HALF_UP));
	}

	/**
	 * Unrounded BTW for an amount at a percentage rate.
	 */
	static BigDecimal exactVat(BigDecimal amount, BigDecimal ratePercent) {
		return amount.multiply(ratePercent).movePointLeft(2);
	}

}