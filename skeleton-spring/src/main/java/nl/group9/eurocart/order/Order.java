package nl.group9.eurocart.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * An order as shown to the customer at creation. Amounts are frozen and never recalculated.
 * The terms version and the consent timestamps are the audit proof of what the customer agreed to.
 */
public record Order(String id, String reference, OrderStatus status, String email, String country,
		String currency, List<OrderLine> lines, BigDecimal subtotal, BigDecimal vat, BigDecimal total,
		Instant createdAt, String termsVersion, Instant termsAcceptedAt) {
}
