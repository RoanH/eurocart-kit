package nl.group9.eurocart.order;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import nl.group9.eurocart.cart.CartService;
import nl.group9.eurocart.cart.CartView;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private static final Pattern POSTAL_CODE = Pattern.compile("^\\d{2}-\\d{3}$");

	private final CartService cart;

	private final Map<String, Order> orders = new ConcurrentHashMap<>();

	public OrderService(CartService cart) {
		this.cart = cart;
	}

	/**
	 * Freezes the current cart into a {@code PENDING_PAYMENT} order and clears the cart.
	 */
	public synchronized Order create(String email, String acceptedTermsVersion, Address shippingAddress) {
		if (email == null || !EMAIL.matcher(email.trim()).matches()) {
			throw new IllegalArgumentException("A valid email address is required");
		}
		if (!Terms.CURRENT_VERSION.equals(acceptedTermsVersion)) {
			throw new IllegalArgumentException("The current Terms & Conditions (" + Terms.CURRENT_VERSION
					+ ") must be accepted");
		}
		CartView view = cart.view();
		if (view.lines().isEmpty()) {
			throw new IllegalArgumentException("Cart is empty");
		}
		// A delivery address is only needed, and only stored, when something has to be
		// shipped.
		Address address = view.shippingRequired() ? validated(shippingAddress) : null;
		List<OrderLine> lines = view.lines().stream()
				.map(line -> new OrderLine(line.product().id(), line.product().name(), line.quantity(),
						line.lineTotal(), line.vatRate(), line.vat()))
				.toList();
		Instant now = Instant.now();
		UUID id = UUID.randomUUID();
		String reference = "EC-" + id.toString().substring(0, 8).toUpperCase();
		Order order = new Order(id.toString(), reference, OrderStatus.PENDING_PAYMENT, email.trim(),
				"PL", "PLN", lines, view.subtotal(), view.shipping(), view.shippingVatRate(),
				view.shippingVat(), view.orderVat(), view.orderTotal(), address, view.freeShipping(),
				view.freeShippingThreshold(), now, Terms.CURRENT_VERSION, now);
		orders.put(order.id(), order);
		cart.clear();
		return order;
	}

	public Order find(String id) {
		Order order = orders.get(id);
		if (order == null) {
			throw new UnknownOrderException(id);
		}
		return order;
	}

	private static Address validated(Address address) {
		if (address == null) {
			throw new IllegalArgumentException("A shipping address is required for physical products");
		}
		String fullName = required(address.fullName(), "Full name");
		String street = required(address.street(), "Street and house number");
		String postalCode = required(address.postalCode(), "Postal code");
		String city = required(address.city(), "City");
		if (!POSTAL_CODE.matcher(postalCode).matches()) {
			throw new IllegalArgumentException("Postal code must look like 00-000");
		}
		return new Address(fullName, street, postalCode, city);
	}

	private static String required(String value, String label) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(label + " is required for the shipping address");
		}
		return value.trim();
	}

}