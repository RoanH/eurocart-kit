package nl.group9.eurocart.cart;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.Category;
import nl.group9.eurocart.catalogue.Country;

/**
 * BTW (VAT) percentages per country and product category.
 */
public final class VatRates {

	private VatRates() {
	}

	/**
	 * BTW percentage on shipping costs: the standard (high) rate of the country, except
	 * Switzerland which uses its reduced (low) rate. Independent of the products in the order.
	 */
	public static BigDecimal shippingRate(Country country) {
		return new BigDecimal(switch (country) {
			case NL -> "21";
			case DE -> "19";
			case FR -> "20";
			case PL -> "23";
			case CH -> "2.6";
			case GB -> "20";
		});
	}

	public static BigDecimal rate(Country country, Category category) {
		return new BigDecimal(switch (country) {
			case NL -> switch (category) {
				case LAPTOP -> "21";
				case BOOK, EBOOK, FOOD, MEDICINE -> "9";
			};
			case DE -> switch (category) {
				case LAPTOP, MEDICINE -> "19";
				case BOOK, EBOOK, FOOD -> "7";
			};
			case FR -> switch (category) {
				case LAPTOP -> "20";
				case BOOK, EBOOK, FOOD -> "5.5";
				case MEDICINE -> "2.1";
			};
			case PL -> switch (category) {
				case LAPTOP -> "23";
				case BOOK, EBOOK, FOOD -> "5";
				case MEDICINE -> "8";
			};
			case CH -> switch (category) {
				case LAPTOP -> "8.1";
				case BOOK, EBOOK, FOOD, MEDICINE -> "2.6";
			};
			case GB -> switch (category) {
				case LAPTOP, MEDICINE -> "20";
				case BOOK, EBOOK, FOOD -> "0";
			};
		});
	}

}