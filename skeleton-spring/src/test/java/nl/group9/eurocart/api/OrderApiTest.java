package nl.group9.eurocart.api;

import nl.group9.eurocart.cart.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {

	private static final String VALID = "{\"email\":\"anna@example.pl\",\"acceptedTermsVersion\":\"2026-10-v1\"}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CartService cartService;

	@BeforeEach
	void emptyCart() {
		cartService.clear();
	}

	private String createOrder(String body) throws Exception {
		String json = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(json, "$.id");
	}

	@Test
	void createsPendingOrderFromCartAndClearsCart() throws Exception {
		cartService.add("EBK-001", 2);
		var cart = cartService.view();

		mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(VALID))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
				.andExpect(jsonPath("$.reference").value(startsWith("EC-")))
				.andExpect(jsonPath("$.email").value("anna@example.pl"))
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.country").value("PL"))
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.lines[0].productId").value("EBK-001"))
				.andExpect(jsonPath("$.lines[0].quantity").value(2))
				.andExpect(jsonPath("$.lines[0].vatRate").value(5))
				.andExpect(jsonPath("$.subtotal").value(cart.subtotal().doubleValue()))
				.andExpect(jsonPath("$.vat").value(cart.vat().doubleValue()))
				.andExpect(jsonPath("$.total").value(cart.subtotalInclVAT().doubleValue()));

		mockMvc.perform(get("/api/cart")).andExpect(jsonPath("$.lines.length()").value(0));
	}

	@Test
	void getReturnsStoredOrderUnaffectedByLaterCartChanges() throws Exception {
		cartService.add("EBK-002", 1);
		var expected = cartService.view();
		String id = createOrder(VALID);
		cartService.add("EBK-001", 3);

		mockMvc.perform(get("/api/orders/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.total").value(expected.subtotalInclVAT().doubleValue()));
	}

	@Test
	void invalidEmailGivesBadRequest() throws Exception {
		cartService.add("EBK-001", 1);
		mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"not-an-email\",\"acceptedTermsVersion\":\"2026-10-v1\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void orderStoresAcceptedTermsVersionAndTimestamps() throws Exception {
		cartService.add("EBK-001", 1);
		mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(VALID))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.termsVersion").value("2026-10-v1"))
				.andExpect(jsonPath("$.termsAcceptedAt").isNotEmpty());
	}

	@Test
	void missingOrWrongTermsVersionGivesBadRequestAndKeepsCart() throws Exception {
		cartService.add("EBK-001", 1);
		for (String body : new String[] {
				"{\"email\":\"anna@example.pl\"}",
				"{\"email\":\"anna@example.pl\",\"acceptedTermsVersion\":\"1999-01-v0\"}" }) {
			mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
					.andExpect(status().isBadRequest());
		}
		mockMvc.perform(get("/api/cart")).andExpect(jsonPath("$.lines.length()").value(1));
	}

	@Test
	void exposesCurrentTermsVersion() throws Exception {
		mockMvc.perform(get("/api/terms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version").value("2026-10-v1"))
				.andExpect(jsonPath("$.url").value("/terms.html"));
	}

	@Test
	void emptyCartGivesBadRequest() throws Exception {
		mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(VALID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Cart is empty"));
	}

	@Test
	void unknownOrderGivesNotFound() throws Exception {
		mockMvc.perform(get("/api/orders/does-not-exist")).andExpect(status().isNotFound());
	}

}
