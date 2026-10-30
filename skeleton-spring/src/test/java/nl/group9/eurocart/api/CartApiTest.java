package nl.group9.eurocart.api;

import nl.group9.eurocart.cart.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CartService cartService;

	@BeforeEach
	void emptyCart() {
		cartService.clear();
	}

	@Test
	void listsTheCatalogueWithoutFood() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(41))
				.andExpect(jsonPath("$[?(@.category == 'FOOD')]").isEmpty());
	}

	@Test
	void foodCannotBeAddedToTheCart() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"FOD-002\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void addsItemAndReturnsCartWithSubtotal() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"BOK-001\",\"quantity\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.lines[0].product.id").value("BOK-001"))
				.andExpect(jsonPath("$.lines[0].quantity").value(2))
				.andExpect(jsonPath("$.lines[0].lineTotal").value(108.70))
				.andExpect(jsonPath("$.subtotal").value(108.70));
	}

	@Test
	void quantityDefaultsToOne() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"MED-001\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines[0].quantity").value(1))
				.andExpect(jsonPath("$.subtotal").value(10.83));
	}

	@Test
	void removesItem() throws Exception {
		cartService.add("LAP-001", 1);
		cartService.add("BOK-001", 1);

		mockMvc.perform(delete("/api/cart/items/LAP-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.lines[0].product.id").value("BOK-001"));
	}

	@Test
	void clearsCart() throws Exception {
		cartService.add("LAP-001", 1);

		mockMvc.perform(delete("/api/cart"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines.length()").value(0))
				.andExpect(jsonPath("$.subtotal").value(0));
	}

	@Test
	void unknownProductGivesNotFoundProblem() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"NOPE-999\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Unknown product: NOPE-999"));
	}

	@Test
	void zeroQuantityGivesBadRequestProblem() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"LAP-001\",\"quantity\":0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cartWithPhysicalProductShowsShippingAndFinalTotals() throws Exception {
		// 12.50 EUR = 54.35 PLN, shipping 5.00 EUR = 21.74 PLN at 23%
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"BOK-001\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.shippingRequired").value(true))
				.andExpect(jsonPath("$.shipping").value(21.74))
				.andExpect(jsonPath("$.shippingVatRate").value(23))
				.andExpect(jsonPath("$.orderVat").value(7.72))
				.andExpect(jsonPath("$.orderTotal").value(83.81));
	}

}