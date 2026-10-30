package nl.group9.eurocart.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * An order as shown to the customer at creation. Amounts are frozen and never recalculated.
 * The terms version and the consent timestamps are the audit proof of what the customer agreed to.
 *
 * @param subtotal products excluding BTW
 * @param shipping shipping costs excluding BTW, zero for e-book-only orders
 * @param shippingVatRate BTW percentage applied to the shipping costs
 * @param shippingVat BTW on the shipping costs, rounded for display only
 * @param vat BTW over products and shipping, computed unrounded and rounded once
 * @param total subtotal + shipping + vat
 * @param shippingAddress delivery address, {@code null} when the order has no physical products
 */
public record Order(String id, String reference, OrderStatus status, String email, String country,
                String currency, List<OrderLine> lines, BigDecimal subtotal, BigDecimal shipping,
                BigDecimal shippingVatRate, BigDecimal shippingVat, BigDecimal vat, BigDecimal total,
                Address shippingAddress, Instant createdAt, String termsVersion, Instant termsAcceptedAt) {
}