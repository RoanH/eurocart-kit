package nl.group9.eurocart.catalogue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogueTest {

	private final ProductCatalogue catalogue = new ProductCatalogue(new ObjectMapper());

	@Test
	void listsOnlyTenEbooksWithUniqueIds() {
		List<Product> products = catalogue.all();

		assertThat(products).hasSize(10);
		assertThat(products).extracting(Product::category).containsOnly(Category.EBOOK);
		assertThat(products).extracting(Product::id).doesNotHaveDuplicates();
	}

	@Test
	void ebooksAreAllDutch() {
		Set<Country> countries = catalogue.all().stream()
				.map(Product::countryOfOrigin)
				.collect(Collectors.toSet());

		assertThat(countries).containsExactly(Country.NL);
	}

	@Test
	void everyProductHasNameAndPositivePrice() {
		assertThat(catalogue.all()).allSatisfy(product -> {
			assertThat(product.name()).isNotBlank();
			assertThat(product.price()).isGreaterThan(BigDecimal.ZERO);
		});
	}

	@Test
	void findsProductById() {
		assertThat(catalogue.find("LAP-001")).isPresent()
				.get().extracting(Product::category).isEqualTo(Category.LAPTOP);
		assertThat(catalogue.find("NOPE-999")).isEmpty();
	}

}
