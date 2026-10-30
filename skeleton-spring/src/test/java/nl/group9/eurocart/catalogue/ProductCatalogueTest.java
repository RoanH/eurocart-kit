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
	void listsAllSoldProductsWithUniqueIds() {
		List<Product> products = catalogue.all();

		assertThat(products).hasSize(41);
		assertThat(products).extracting(Product::id).doesNotHaveDuplicates();
		assertThat(products).extracting(Product::category).contains(Category.EBOOK, Category.LAPTOP);
	}

	@Test
	void foodIsNotSoldYet() {
		assertThat(catalogue.all()).extracting(Product::category).doesNotContain(Category.FOOD);
		assertThat(catalogue.find("FOD-002")).isEmpty();
		assertThat(catalogue.exactPrice("FOD-002")).isNull();
	}

	@Test
	void onlyEbooksAreNotPhysical() {
		assertThat(Category.EBOOK.physical()).isFalse();
		assertThat(List.of(Category.LAPTOP, Category.BOOK, Category.FOOD, Category.MEDICINE))
				.allMatch(Category::physical);
	}

	@Test
	void ebooksAreAllDutch() {
		Set<Country> countries = catalogue.all().stream()
				.filter(product -> product.category() == Category.EBOOK)
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