package nl.group9.eurocart.catalogue;

public enum Category {
	LAPTOP,
	BOOK,
	FOOD,
	MEDICINE,
	EBOOK;

	/**
	 * Physical products have to be shipped; e-books are delivered by email.
	 */
	public boolean physical() {
		return this != EBOOK;
	}
}