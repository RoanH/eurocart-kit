# EuroCart – Implementation status

What is built against [slices-checkout.md](./slices-checkout.md). Update this file when a slice lands.

## Done: Theme 1 (slices 1.1–1.3)

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

## Done: Theme 2 (slices 2.1–2.2; 2.3 removed for now)
- `static/terms.html`: versioned T&C text (`2026-10-v1`) for e-books in Poland. Seller details are placeholders to complete before go-live. Linked from the checkout checkbox (opens in a new tab).
- `order/Terms.CURRENT_VERSION` holds the version; `GET /api/terms` returns `{version, url}` and the front-end sends that version on submit.
- `POST /api/orders` body is now `{email, acceptedTermsVersion}`. A missing or non-current version → 400. `Order` stores `termsVersion` and `termsAcceptedAt`.
- Slice 2.3 (withdrawal-waiver checkbox and consent) was built and then removed again on request; still to confirm with Compliance.
- Tests: new cases in `api/OrderApiTest`.

## Deviations from slices-checkout.md
- **No name field.** The product owner does not need the customer's name; the form, the API request and `Order` carry only `email`. Slices 1.1/1.2 still say "name and email".
- Slice 1.3 has no "Pay now" button (that arrives with 3.1).

## Not done yet
Themes 3–4: payment screen and payment endpoint (3.x), outbox, no-delivery-before-payment and download links (4.x).
