package nl.group9.eurocart.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * An order as shown to the customer at creation. Amounts are frozen and never recalculated.
 */
public record Order(String id, String reference, OrderStatus status, String email, String country,
		String currency, List<OrderLine> lines, BigDecimal subtotal, BigDecimal vat, BigDecimal total,
		Instant createdAt) {
}
