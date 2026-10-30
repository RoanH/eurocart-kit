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

## Done: Physical products, shipping and address (Poland only)
- **Catalogue:** `GET /api/products` and the cart now cover all 51 products, not just e-books. `Category.physical()` is true for everything except `EBOOK`.
- **Shipping (R8a, R8b, R9):** a flat EUR 5.00 per order, excl. BTW, converted to PLN (21.74). It is charged once, when the cart holds at least one physical product, also in a mixed cart with e-books. An e-book-only cart pays no shipping. Shipping BTW uses `VatRates.shippingRate(country)`: the high rate everywhere (PL 23%), Switzerland the low rate (2.6%), independent of the products.
- **Totals:** `CartView` keeps `subtotal`, `vat` and `subtotalInclVAT` for the products only (the cart panel does not show shipping, per Sales). The new fields `shippingRequired`, `shipping`, `shippingVatRate`, `shippingVat`, `orderVat` and `orderTotal` give the real amount to pay. `orderVat` adds the unrounded BTW of the products and of the shipping and rounds once (R22).
- **Order:** `Order` stores `shipping`, `shippingVatRate`, `shippingVat`, `vat` (incl. shipping), `total` and `shippingAddress`, frozen at order time.
- **Address:** `CreateOrderRequest` has an optional `shippingAddress` (`Address`: `fullName`, `street`, `postalCode` as `NN-NNN`, `city`). Required only if the cart holds physical products (missing or invalid → 400, cart kept). For e-book-only orders it is ignored and not stored. Country is always Poland.
- **Front-end:** the checkout step shows subtotal, shipping (with its BTW), BTW and total to pay. The address block only appears for physical carts. The summary shows the shipping row and the address.
- Tests: new cases in `CartServiceTest`, `CartApiTest`, `OrderApiTest` and `ProductCatalogueTest`.
- Open: customer names and addresses are kept in memory (A25); the T&Cs text (`2026-10-v1`) still only covers e-books and needs shipping and withdrawal rules for physical goods, probably a new version.

## Deviations from slices-checkout.md
- **No name field.** The product owner does not need the customer's name; the form, the API request and `Order` carry only `email`. Slices 1.1/1.2 still say "name and email".
- Slice 1.3 has no "Pay now" button (that arrives with 3.1).

## Not done yet
Themes 3–4: payment screen and payment endpoint (3.x), outbox, no-delivery-before-payment and download links (4.x).