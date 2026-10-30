package nl.group9.eurocart.cart;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Currency;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    /**
     * Flat shipping costs per order in EUR, excluding BTW, whatever the products, quantities or weight.
     * Orders with only e-books have no shipping costs.
     */
    private static final BigDecimal SHIPPING_EUR = new BigDecimal("5.00");

    private final ProductCatalogue catalogue;

    private final Map<String, Integer> quantitiesByProductId = new LinkedHashMap<>();

    public CartService(ProductCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    /**
     * Adds {@code quantity} of a product. Adding an existing product increases its quantity.
     */
    public synchronized CartView add(String productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (catalogue.find(productId).isEmpty()) {
            throw new UnknownProductException(productId);
        }
        quantitiesByProductId.merge(productId, quantity, Integer::sum);
        return view();
    }

    /**
     * Removes a product entirely, whatever its quantity. Unknown ids are ignored.
     */
    public synchronized CartView remove(String productId) {
        quantitiesByProductId.remove(productId);
        return view();
    }

    public synchronized CartView clear() {
        quantitiesByProductId.clear();
        return view();
    }

    public synchronized CartView view() {
        List<CartLine> lines = quantitiesByProductId.entrySet().stream()
                .map(entry -> CartLine.of(product(entry.getKey()), entry.getValue()))
                .toList();
        BigDecimal subtotal = lines.stream()
                .map(CartLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Sum the unrounded BTW of every line and round only once.
        BigDecimal vat = lines.stream()
                .map(line -> CartLine.exactVat(line.lineTotal(), line.vatRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean shippingRequired = lines.stream().anyMatch(line -> line.product().category().physical());

        // Free shipping threshold: physical products only, incl. BTW (rounded once), after discount
        // (there are no discounts yet, so the line totals are already the discounted values).
        List<CartLine> physical = lines.stream().filter(line -> line.product().category().physical()).toList();
        BigDecimal physicalNet = physical.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal physicalVat = physical.stream()
                .map(line -> CartLine.exactVat(line.lineTotal(), line.vatRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal freeShippingBasis = physicalNet.add(physicalVat);
        BigDecimal threshold = FreeShipping.threshold(Country.PL);
        // "Above the threshold": an order of exactly the threshold still pays shipping.
        boolean freeShipping = shippingRequired && freeShippingBasis.compareTo(threshold) > 0;
        BigDecimal amountForFreeShipping = shippingRequired && !freeShipping
                ? threshold.subtract(freeShippingBasis).add(new BigDecimal("0.01"))
                : BigDecimal.ZERO.setScale(2);

        BigDecimal shipping = shippingRequired && !freeShipping
                ? Currency.EUR.convertTo(Currency.LOCAL, SHIPPING_EUR)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal shippingVatRate = VatRates.shippingRate(Country.PL);
        BigDecimal exactShippingVat = CartLine.exactVat(shipping, shippingVatRate);
        BigDecimal shippingVat = exactShippingVat.setScale(2, RoundingMode.HALF_UP);
        // Products and shipping BTW are added up unrounded and rounded once, like the rest of the order.
        BigDecimal orderVat = lines.stream()
                .map(line -> CartLine.exactVat(line.lineTotal(), line.vatRate()))
                .reduce(exactShippingVat, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal orderTotal = subtotal.add(shipping).add(orderVat);
        return new CartView(lines, subtotal, vat, subtotal.add(vat), shippingRequired, shipping,
                shippingVatRate, shippingVat, orderVat, orderTotal, threshold, freeShippingBasis,
                freeShipping, amountForFreeShipping);
    }

    private Product product(String productId) {
        return catalogue.find(productId).orElseThrow(() -> new UnknownProductException(productId));
    }

}