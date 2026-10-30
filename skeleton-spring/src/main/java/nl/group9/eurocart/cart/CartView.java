package nl.group9.eurocart.cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * The cart as shown to the customer.
 * <p>
 * {@code subtotal}, {@code vat} and {@code subtotalInclVAT} cover the products only, so the cart panel
 * can show them without shipping (shipping is only revealed at checkout). The remaining fields describe
 * what is actually paid: shipping and the final BTW and total, where the BTW of the products and of the
 * shipping is added up unrounded and rounded once.
 *
 * @param shippingRequired whether the cart holds a physical product, so it must be shipped and needs an address
 * @param shipping shipping costs excluding BTW; zero when nothing has to be shipped
 * @param shippingVatRate BTW percentage on the shipping costs
 * @param shippingVat BTW on the shipping costs, rounded for display only
 * @param orderVat BTW over products and shipping together, rounded once
 * @param orderTotal total to pay: subtotal + shipping + orderVat
 * @param freeShippingThreshold order value above which shipping is free, in the local currency
 * @param freeShippingBasis the order value compared with the threshold: physical products only, after discount,
 *        including BTW (e-books do not count)
 * @param freeShipping whether the order qualifies for free shipping, so {@code shipping} is zero
 * @param amountForFreeShipping how much more physical products (incl. BTW) the customer must add to get free
 *        shipping; zero when nothing is shipped or shipping is already free
 */
public record CartView(List<CartLine> lines, BigDecimal subtotal, BigDecimal vat, BigDecimal subtotalInclVAT,
		boolean shippingRequired, BigDecimal shipping, BigDecimal shippingVatRate, BigDecimal shippingVat,
		BigDecimal orderVat, BigDecimal orderTotal, BigDecimal freeShippingThreshold, BigDecimal freeShippingBasis,
		boolean freeShipping, BigDecimal amountForFreeShipping) {
}