package nl.group9.eurocart.catalogue;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductCatalogue {

    private final Map<String, Product> productsById;

    /**
     * Unrounded local-currency price per product id. {@link Product#price()} is rounded to cents for display;
     * amounts are calculated from this exact price and rounded only when they are reported.
     */
    private final Map<String, BigDecimal> exactPricesById = new LinkedHashMap<>();

    public ProductCatalogue(ObjectMapper objectMapper) {
        this.productsById = load(objectMapper);
    }

    public List<Product> all() {
        return List.copyOf(productsById.values());
    }

    public Optional<Product> find(String id) {
        return Optional.ofNullable(productsById.get(id));
    }

    /**
     * The unrounded local-currency price (excl. BTW) of a known product.
     */
    public BigDecimal exactPrice(String id) {
        return exactPricesById.get(id);
    }

    private Map<String, Product> load(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("catalogue.json").getInputStream()) {
            List<Product> products = objectMapper.readValue(in, new TypeReference<List<Product>>() {
            });
            Map<String, Product> byId = new LinkedHashMap<>();
            for (Product catalogued : products) {
                Product product = inLocalCurrency(catalogued);
                exactPricesById.put(product.id(), Currency.EUR.convertExact(Currency.LOCAL, catalogued.price()));
                if (byId.put(product.id(), product) != null) {
                    throw new IllegalStateException("Duplicate product id in catalogue.json: " + product.id());
                }
            }
            return byId;
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read catalogue.json", ex);
        }
    }

    /**
     * catalogue.json prices are in EUR; the shop shows and sells in the local currency. The price on the
     * returned product is only the rounded display price.
     */
    private static Product inLocalCurrency(Product product) {
        return new Product(product.id(), product.name(), product.category(),
                Currency.EUR.convertTo(Currency.LOCAL, product.price()), product.countryOfOrigin());
    }

}