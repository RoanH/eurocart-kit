package nl.group9.eurocart.cart;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.Country;

/**
 * Free shipping thresholds (SAL-1). An order ships for free when its value is <em>above</em> the
 * threshold of the delivery country. The value is measured after discount and including BTW,
 * over physical products only: e-books do not count because they have no shipping.
 */
public final class FreeShipping {

        private FreeShipping() {
        }

        /**
         * The threshold in the currency of the country itself (EUR for NL/DE/FR, PLN, CHF, GBP), so it is
         * deliberately not converted: PL is 250 PLN, not the euro equivalent.
         */
        public static BigDecimal threshold(Country country) {
                return new BigDecimal(switch (country) {
                        case NL, DE, FR -> "50.00";
                        case PL -> "250.00";
                        case CH -> "100.00";
                        case GB -> "45.00";
                });
        }

}
