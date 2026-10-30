# EuroCart – Implementation status

What is built against [slices-checkout.md](./slices-checkout.md). Update this file when a slice lands.

## Done: Theme 1 (slices 1.1–1.3), plus a placeholder for 2.2

Commit `93b606d`. Scope: e-books, Poland only (PLN, PL VAT), guest checkout, everything in memory.

### Backend
- `order/` package: `Order`, `OrderLine`, `OrderStatus` (only `PENDING_PAYMENT` so far), `OrderService`, `UnknownOrderException`.
- `OrderService.create(email)` validates the email (simple regex), refuses an empty cart (`IllegalArgumentException` "Cart is empty" → 400), snapshots `CartService.view()` into an `Order` (lines with productId, name, quantity, lineTotal, vatRate, vat; plus subtotal, vat, total, country `PL`, currency `PLN`, `createdAt`), stores it in a `ConcurrentHashMap`, then clears the cart. Amounts are never recalculated afterwards.
- `api/OrderController`: `POST /api/orders` body `{email}` → 201; `GET /api/orders/{id}` → order or 404 (`UnknownOrderException` handler in `ApiExceptionHandler`).
- Reference is `EC-` + first 8 chars of the UUID, upper-case.
- Tests: `api/OrderApiTest` (create + cart cleared, snapshot unaffected by later cart changes, invalid email 400, empty cart 400, unknown id 404).

### Front-end (`static/index.html`)
- The cart panel swaps between three steps in place (`showStep`): cart → checkout → summary. Products stay visible on the left.
- Checkout step: email + confirm email (format and match checked client-side, errors only after touching a field), an unticked "I accept the Terms & Conditions" checkbox, and **Place order**, which is disabled until the emails are valid and the box is ticked.
- Summary step: order reference, delivery email, lines, subtotal (VAT base), BTW, total in PLN, "no shipping costs" note, **Continue shopping**.

## Deviations from slices-checkout.md
- **No name field.** The product owner does not need the customer's name; the form, the API request and `Order` carry only `email`. Slices 1.1/1.2 still say "name and email".
- **T&C checkbox is UI-only for now.** It is a stand-in for slice 2.2: no `acceptedTermsVersion`, no `termsVersion`/`termsAcceptedAt` on the order, no backend enforcement, no link to a terms page (2.1 not built).
- Slice 1.3 has no "Pay now" button (that arrives with 3.1).

## Not done yet
Themes 2–4: terms page (2.1), backend-enforced acceptance with version and timestamp (2.2), withdrawal-waiver consent (2.3, to confirm with Compliance), payment screen and payment endpoint (3.x), outbox, no-delivery-before-payment and download links (4.x).

## Known pre-existing test failures (not caused by this work)
The catalogue only exposes EBOOK products (`ProductCatalogue.all()`), but these tests still expect all 51 products: `CartApiTest.listsAllProducts`, `ProductCatalogueTest.containsFiftyOneProductsWithUniqueIds`, `.coversAllSixCountries`, `.hasTenProductsPerCategoryPlusTheFluteSheetMusic`.
