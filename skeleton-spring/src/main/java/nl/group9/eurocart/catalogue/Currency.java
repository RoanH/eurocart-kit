package nl.group9.eurocart.catalogue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Currencies with a hardcoded value in euro.
 */
public enum Currency {

	EUR("1.00"),
	GBP("1.20"),
	CHF("1.09"),
	PLN("0.23");

	/**
	 * The currency the shop sells in. We currently only operate in Poland.
	 */
	public static final Currency LOCAL = PLN;

	private final BigDecimal euroValue;

	Currency(String euroValue) {
		this.euroValue = new BigDecimal(euroValue);
	}

	/**
	 * Converts an amount in this currency to the target currency without rounding to cents, so it can be
	 * multiplied and taxed first. The quotient is not always finite (1 / 0.23), so it is kept at 34 significant
	 * digits, far below a cent. Round the final amounts that are reported to the customer.
	 */
	public BigDecimal convertExact(Currency target, BigDecimal amount) {
		return amount.multiply(euroValue).divide(target.euroValue, MathContext.DECIMAL128);
	}

	/**
	 * Converts an amount in this currency to the target currency, rounded to 2 decimals.
	 * The conversion is a single multiplication and division, so it rounds only once.
	 */
	public BigDecimal convertTo(Currency target, BigDecimal amount) {
		return amount.multiply(euroValue).divide(target.euroValue, 2, RoundingMode.HALF_UP);
	}

}
