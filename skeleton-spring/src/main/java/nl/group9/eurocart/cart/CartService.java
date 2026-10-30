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
        // Amounts are calculated from the exact local price and rounded only where they are reported.
        Map<String, BigDecimal> exactNets = new LinkedHashMap<>();
        quantitiesByProductId.forEach((id, quantity) ->
                exactNets.put(id, catalogue.exactPrice(id).multiply(BigDecimal.valueOf(quantity))));
        List<CartLine> lines = quantitiesByProductId.entrySet().stream()
                .map(entry -> CartLine.of(product(entry.getKey()), entry.getValue(), exactNets.get(entry.getKey())))
                .toList();
        BigDecimal exactSubtotal = exactNets.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal exactProductVat = lines.stream()
                .map(line -> CartLine.exactVat(exactNets.get(line.product().id()), line.vatRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = round(exactSubtotal);
        BigDecimal vat = round(exactProductVat);

        boolean shippingRequired = lines.stream().anyMatch(line -> line.product().category().physical());

        // Free shipping threshold: physical products only, incl. BTW (rounded once), after discount
        // (there are no discounts yet, so the line amounts are already the discounted values).
        List<CartLine> physical = lines.stream().filter(line -> line.product().category().physical()).toList();
        BigDecimal exactPhysicalNet = physical.stream()
                .map(line -> exactNets.get(line.product().id()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal exactPhysicalVat = physical.stream()
                .map(line -> CartLine.exactVat(exactNets.get(line.product().id()), line.vatRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freeShippingBasis = round(exactPhysicalNet).add(round(exactPhysicalVat));
        BigDecimal threshold = FreeShipping.threshold(Country.PL);
        // "Above the threshold": an order of exactly the threshold still pays shipping.
        boolean freeShipping = shippingRequired && freeShippingBasis.compareTo(threshold) > 0;
        BigDecimal amountForFreeShipping = shippingRequired && !freeShipping
                ? threshold.subtract(freeShippingBasis).add(new BigDecimal("0.01"))
                : BigDecimal.ZERO.setScale(2);

        BigDecimal exactShipping = shippingRequired && !freeShipping
                ? Currency.EUR.convertExact(Currency.LOCAL, SHIPPING_EUR)
                : BigDecimal.ZERO;
        BigDecimal shipping = round(exactShipping);
        BigDecimal shippingVatRate = VatRates.shippingRate(Country.PL);
        BigDecimal exactShippingVat = CartLine.exactVat(exactShipping, shippingVatRate);
        BigDecimal shippingVat = round(exactShippingVat);
        // Products and shipping BTW are added up unrounded and rounded once, like the rest of the order.
        BigDecimal orderVat = round(exactProductVat.add(exactShippingVat));
        BigDecimal orderTotal = subtotal.add(shipping).add(orderVat);
        return new CartView(lines, subtotal, vat, subtotal.add(vat), shippingRequired, shipping,
                shippingVatRate, shippingVat, orderVat, orderTotal, threshold, freeShippingBasis,
                freeShipping, amountForFreeShipping);
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private Product product(String productId) {
        return catalogue.find(productId).orElseThrow(() -> new UnknownProductException(productId));
    }

}