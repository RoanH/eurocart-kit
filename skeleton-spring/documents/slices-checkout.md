# EuroCart – Checkout slices (delivery details, payment, compliance)

Scope: e-books only, Poland only (PLN, PL VAT). E-books are delivered by email. Hackathon setup: everything in memory, no real email or payment provider.

## Decisions

- **Guest checkout only.** No account, login or registration. The customer only gives name and email per order (resolves A3).
- **Payment before delivery.** An e-book is never sent before the order is paid.
- **Amounts are frozen at order time.** The order stores the totals as shown and is never recalculated (R19, R36).

## Theme 1 – Capture delivery details

### Slice 1.1 – Checkout step with email capture
- "Checkout" button on the cart opens a guest checkout form: full name, email, confirm email.
- Delivery country is shown as **Poland** (read-only).
- Client-side validation: valid email format, both emails match.
- **Demo:** cart → checkout → form rejects a bad or mismatched email.

### Slice 1.2 – Create an order from the cart
- `POST /api/orders` with `{name, email}` returns an order with status `PENDING_PAYMENT`.
- The order stores a snapshot of the cart: lines, subtotal, VAT, total, currency (PLN) and VAT rates used.
- Server-side validation: invalid email → 400 `ProblemDetail`; empty cart → 400.
- The cart is cleared once the order is created.
- `GET /api/orders/{id}` returns the stored order.
- **Demo:** place an order and `GET` it back with identical amounts.

### Slice 1.3 – Order summary before payment
- After creating the order the customer sees: order reference, delivery email, lines, VAT base, VAT amount and total in PLN (R17).
- Note: "E-books are delivered by email, no shipping costs" (R9).
- **Demo:** summary matches the cart totals to the grosz.

## Theme 2 – Compliance

### Slice 2.1 – Terms & Conditions page
- Static `/terms.html` with a versioned T&C text (e.g. `2026-10-v1`).
- Covers: seller identity, delivery of e-books by email after payment, prices in PLN incl. VAT, withdrawal right for digital content.
- Linked from the checkout form.
- **Demo:** link opens the terms page.

### Slice 2.2 – Accepting the T&Cs is required
- Unticked "I accept the Terms & Conditions" checkbox on checkout; "Place order" stays disabled until ticked.
- Backend enforces it: `POST /api/orders` without `acceptedTermsVersion` matching the current version → 400.
- The order stores `termsVersion` and `termsAcceptedAt` (audit proof).
- **Demo:** API call without acceptance → 400; with acceptance → order shows version and timestamp.

### Slice 2.3 – Consent for immediate delivery of digital content
- Second, separate, unticked checkbox: "I agree that delivery starts immediately after payment and I understand I lose my 14-day right of withdrawal."
- Based on EU Consumer Rights Directive art. 16(m), applicable in Poland.
- Enforced by the backend and stored on the order with a timestamp.
- **Demo:** an order can't be placed without this consent.
- **To confirm with Compliance:** not in the stakeholder memos.

## Theme 3 – Simulated payment

### Slice 3.1 – Payment screen
- "Pay now" on the summary opens `/pay.html?order=…`.
- Shows merchant name, order reference and amount in PLN.
- Payment methods: **BLIK** (6-digit code) or **Card** (number, expiry, CVC).
- Only the screen and its format validation; no backend call yet.
- **Demo:** a believable payment page.

### Slice 3.2 – Successful payment
- `POST /api/orders/{id}/payment` moves `PENDING_PAYMENT` → `PAID` and stores `paidAt` and a fake transaction id.
- The amount comes from the stored order, never from the client.
- Paying twice → 409; unknown order → 404.
- Short "Processing…" state, then a confirmation page.
- **Demo:** pay → order status `PAID`.

### Slice 3.3 – Declined payment
- Test values trigger a decline: BLIK `000000`, card `4000 0000 0000 0002`.
- Order stays `PENDING_PAYMENT`; customer sees "Payment declined, try again".
- **Demo:** decline, then retry successfully.

## Theme 4 – Delivery by email (only after payment)

### Slice 4.1 – Fake email outbox
- When an order becomes `PAID`, an in-memory `Outbox` records an email: recipient, subject, body with the order lines and one download link per e-book.
- `GET /api/outbox` and `/outbox.html` act as the demo inbox.
- **Demo:** pay → open outbox → email is there.

### Slice 4.2 – No delivery before payment
- Core compliance rule, covered by tests:
  - A `PENDING_PAYMENT` or declined order never creates an outbox entry.
  - A paid order creates exactly one entry, even if payment is called again.
- **Demo:** a declined order leaves the outbox empty.

### Slice 4.3 – Download links only for paid orders
- `GET /api/downloads/{token}` returns a placeholder file (small text "e-book").
- Token is random and tied to one order line.
- Unknown token → 404; token of an unpaid order → 403.
- **Demo:** click the link in the outbox and the "book" downloads.

### Slice 4.4 (stretch) – Receipt in the email
- Email includes a receipt: amounts excl. VAT, VAT rate per line, VAT amount and total (R37), plus the accepted terms version.

## Suggested order

1. Walking skeleton: **1.1 → 1.2 → 2.2 → 3.2 → 4.1 → 4.2** (details → accept terms → pay → email).
2. Then: 1.3, 2.1, 2.3, 3.1, 3.3, 4.3, 4.4.

## Open points

- **Withdrawal waiver (2.3):** confirm with Compliance.
- **Payment methods (A1):** BLIK and card are an assumption for Poland.
- **Customer data (A25):** orders hold name and email in memory; retention and privacy not discussed.
