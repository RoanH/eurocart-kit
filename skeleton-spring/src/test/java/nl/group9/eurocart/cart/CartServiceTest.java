package nl.group9.eurocart.cart;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.ProductCatalogue;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartServiceTest {

	private final CartService cart = new CartService(new ProductCatalogue(new ObjectMapper()));

	@Test
	void startsEmpty() {
		CartView view = cart.view();

		assertThat(view.lines()).isEmpty();
		assertThat(view.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void addsProductsAndSumsPricesExcludingVat() {
		cart.add("BOK-001", 2); // 2 x 12.50 EUR
		CartView view = cart.add("FOD-002", 1); // 1 x 1.29 EUR

		assertThat(view.lines()).hasSize(2);
		assertThat(view.subtotal()).isEqualByComparingTo("114.30"); // 26.29 EUR = 114.3043 PLN
	}

	@Test
	void addingSameProductAgainIncreasesQuantity() {
		cart.add("MED-001", 1);
		CartView view = cart.add("MED-001", 3);

		assertThat(view.lines()).singleElement().satisfies(line -> {
			assertThat(line.quantity()).isEqualTo(4);
			assertThat(line.lineTotal()).isEqualByComparingTo("43.30"); // 4 x 2.49 EUR = 43.3043 PLN
		});
	}

	@Test
	void removesProductRegardlessOfQuantity() {
		cart.add("LAP-001", 5);
		cart.add("BOK-010", 1);

		CartView view = cart.remove("LAP-001");

		assertThat(view.lines()).extracting(line -> line.product().id()).containsExactly("BOK-010");
	}

	@Test
	void clearEmptiesTheCart() {
		cart.add("LAP-001", 1);

		CartView view = cart.clear();

		assertThat(view.lines()).isEmpty();
	}

	@Test
	void rejectsUnknownProduct() {
		assertThatThrownBy(() -> cart.add("NOPE-999", 1))
				.isInstanceOf(UnknownProductException.class)
				.hasMessageContaining("NOPE-999");
	}

	@Test
	void rejectsQuantityBelowOne() {
		assertThatThrownBy(() -> cart.add("LAP-001", 0))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void roundsVatOnlyOnceOnTheTotal() {
		cart.add("EBK-001", 1); // 39.09 PLN at 5% = 1.9545
		CartView view = cart.add("EBK-004", 1); // 8.65 PLN at 5% = 0.4325

		// Displayed line BTW is rounded per line: 1.95 + 0.43 = 2.38
		assertThat(view.lines().get(0).vat()).isEqualByComparingTo("1.95");
		assertThat(view.lines().get(1).vat()).isEqualByComparingTo("0.43");
		// The real BTW is computed on the unrounded total: 2.387 -> 2.39
		assertThat(view.vat()).isEqualByComparingTo("2.39");
		assertThat(view.subtotalInclVAT()).isEqualByComparingTo("50.13");
	}

	@Test
	void convertsFromEuroExactlyAndRoundsOnlyTheReportedAmounts() {
		// 3 x 8.99 EUR = 26.97 EUR = 117.2609 PLN; rounding the unit price first would give 3 x 39.09 = 117.27
		CartView view = cart.add("EBK-001", 3);

		assertThat(view.subtotal()).isEqualByComparingTo("117.26");
		assertThat(view.vat()).isEqualByComparingTo("5.86"); // 5.8630
		assertThat(view.subtotalInclVAT()).isEqualByComparingTo("123.12");
		assertThat(view.lines()).singleElement()
				.satisfies(line -> assertThat(line.lineTotal()).isEqualByComparingTo("117.26"));
	}

	@Test
	void largeQuantitiesDoNotAccumulateUnitPriceRoundingErrors() {
		// 25 x 1649 EUR = 41225 EUR = 179239.1304 PLN; rounding the unit price first would give 179239.25
		CartView view = cart.add("LAP-002", 25);

		assertThat(view.subtotal()).isEqualByComparingTo("179239.13");
		assertThat(view.vat()).isEqualByComparingTo("41225.00"); // 23% of 179239.1304
		assertThat(view.subtotalInclVAT()).isEqualByComparingTo("220464.13");
	}

	@Test
	void ebookOnlyCartHasNoShipping() {
		CartView view = cart.add("EBK-001", 2);

		assertThat(view.shippingRequired()).isFalse();
		assertThat(view.shipping()).isEqualByComparingTo("0");
		assertThat(view.orderVat()).isEqualByComparingTo(view.vat());
		assertThat(view.orderTotal()).isEqualByComparingTo(view.subtotalInclVAT());
	}

	@Test
	void physicalProductAddsFlatShippingPerOrder() {
		cart.add("BOK-001", 1);
		CartView view = cart.add("FOD-002", 3); // 54.35 + 16.83 = 71.18 net, well below 250 PLN

		// EUR 5.00 / 0.23 = 21.74 PLN, once per order
		assertThat(view.shippingRequired()).isTrue();
		assertThat(view.freeShipping()).isFalse();
		assertThat(view.shipping()).isEqualByComparingTo("21.74");
		assertThat(view.shippingVatRate()).isEqualByComparingTo("23");
	}

	@Test
	void shippingIsFreeAboveTheThresholdIncludingVat() {
		CartView view = cart.add("LAP-001", 1); // 3908.70 PLN, far above 250 PLN

		assertThat(view.freeShippingThreshold()).isEqualByComparingTo("250.00");
		assertThat(view.freeShipping()).isTrue();
		assertThat(view.shipping()).isEqualByComparingTo("0");
		assertThat(view.shippingVat()).isEqualByComparingTo("0");
		assertThat(view.amountForFreeShipping()).isEqualByComparingTo("0");
		// no shipping: the order costs exactly the products incl. BTW
		assertThat(view.orderTotal()).isEqualByComparingTo(view.subtotalInclVAT());
	}

	@Test
	void thresholdIsMeasuredInclVatNotExclVat() {
		// 55.16 EUR = 239.83 PLN net (below 250) but 251.82 incl. 5% BTW (above 250)
		cart.add("BOK-001", 4);
		CartView view = cart.add("FOD-002", 4);

		assertThat(view.subtotal()).isEqualByComparingTo("239.83");
		assertThat(view.freeShippingBasis()).isEqualByComparingTo("251.82");
		assertThat(view.freeShipping()).isTrue();
		assertThat(view.shipping()).isEqualByComparingTo("0");
	}

	@Test
	void belowTheThresholdNormalShippingAppliesAndTheRemainingAmountIsShown() {
		cart.add("BOK-001", 4);
		CartView view = cart.add("FOD-002", 3); // 53.87 EUR = 234.22 net + 11.71 BTW = 245.93

		assertThat(view.freeShippingBasis()).isEqualByComparingTo("245.93");
		assertThat(view.freeShipping()).isFalse();
		assertThat(view.shipping()).isEqualByComparingTo("21.74");
		// "above" 250: the customer needs to reach 250.01
		assertThat(view.amountForFreeShipping()).isEqualByComparingTo("4.08");
	}

	@Test
	void ebooksDoNotCountTowardsTheThreshold() {
		cart.add("EBK-001", 10); // 390.90 PLN of e-books
		CartView view = cart.add("BOK-001", 1); // only 57.07 incl. BTW counts

		assertThat(view.freeShippingBasis()).isEqualByComparingTo("57.07");
		assertThat(view.freeShipping()).isFalse();
		assertThat(view.shipping()).isEqualByComparingTo("21.74");
		assertThat(view.amountForFreeShipping()).isEqualByComparingTo("192.94");
	}

	@Test
	void ebookOnlyCartHasNothingToReachForFreeShipping() {
		CartView view = cart.add("EBK-001", 10);

		assertThat(view.shippingRequired()).isFalse();
		assertThat(view.freeShipping()).isFalse();
		assertThat(view.amountForFreeShipping()).isEqualByComparingTo("0");
	}

	@Test
	void thresholdsPerCountryAreInTheLocalCurrency() {
		assertThat(FreeShipping.threshold(nl.group9.eurocart.catalogue.Country.NL)).isEqualByComparingTo("50");
		assertThat(FreeShipping.threshold(nl.group9.eurocart.catalogue.Country.DE)).isEqualByComparingTo("50");
		assertThat(FreeShipping.threshold(nl.group9.eurocart.catalogue.Country.FR)).isEqualByComparingTo("50");
		assertThat(FreeShipping.threshold(nl.group9.eurocart.catalogue.Country.PL)).isEqualByComparingTo("250");
		assertThat(FreeShipping.threshold(nl.group9.eurocart.catalogue.Country.CH)).isEqualByComparingTo("100");
		assertThat(FreeShipping.threshold(nl.group9.eurocart.catalogue.Country.GB)).isEqualByComparingTo("45");
	}

	@Test
	void mixedCartWithEbookAndPhysicalProductPaysShipping() {
		cart.add("EBK-001", 1);
		CartView view = cart.add("BOK-001", 1);

		assertThat(view.shippingRequired()).isTrue();
		assertThat(view.shipping()).isEqualByComparingTo("21.74");
	}

	@Test
	void shippingVatIsAddedUnroundedAndRoundedOnceWithProductVat() {
		CartView view = cart.add("BOK-001", 1); // 54.35 PLN at 5% = 2.7175, shipping 21.74 at 23% = 5.0002

		assertThat(view.shippingVat()).isEqualByComparingTo("5.00");
		assertThat(view.orderVat()).isEqualByComparingTo("7.72"); // 7.7177
		assertThat(view.orderTotal()).isEqualByComparingTo("83.81"); // 54.35 + 21.74 + 7.72
	}

	@Test
	void shippingVatIsHighRateEverywhereExceptSwitzerlandWhichIsLow() {
		assertThat(VatRates.shippingRate(nl.group9.eurocart.catalogue.Country.NL)).isEqualByComparingTo("21");
		assertThat(VatRates.shippingRate(nl.group9.eurocart.catalogue.Country.DE)).isEqualByComparingTo("19");
		assertThat(VatRates.shippingRate(nl.group9.eurocart.catalogue.Country.FR)).isEqualByComparingTo("20");
		assertThat(VatRates.shippingRate(nl.group9.eurocart.catalogue.Country.PL)).isEqualByComparingTo("23");
		assertThat(VatRates.shippingRate(nl.group9.eurocart.catalogue.Country.CH)).isEqualByComparingTo("2.6");
		assertThat(VatRates.shippingRate(nl.group9.eurocart.catalogue.Country.GB)).isEqualByComparingTo("20");
	}

	@Test
	void appliesPolishRates() {
		CartView view = cart.add("LAP-003", 1); // 5213.04 PLN, laptop at 23%

		assertThat(view.lines()).singleElement().satisfies(line -> {
			assertThat(line.vatRate()).isEqualByComparingTo("23");
			assertThat(line.vat()).isEqualByComparingTo("1199.00");
		});
	}

}