package nl.group9.eurocart.order;

public class UnknownOrderException extends RuntimeException {

	public UnknownOrderException(String orderId) {
		super("Unknown order: " + orderId);
	}

}
