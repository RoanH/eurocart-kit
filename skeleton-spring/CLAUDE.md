# EuroCart (Spring Boot skeleton)

Checkout for a Dutch web shop selling to six countries (NL, DE, FR, PL, CH, GB). Workshop starter for Elephant Carpaccio: build in thin vertical slices.

## Run

Java 21, Spring Boot 4.1, Maven wrapper.

```bash
./mvnw spring-boot:run   # http://localhost:8080
./mvnw test
```

## What it currently does

- **Catalogue** (`catalogue/`): loads 51 products from `src/main/resources/catalogue.json` (EUR, excl. VAT) and converts prices to PLN on load (`Currency.LOCAL`, hardcoded rates in `Currency`). `GET /api/products` returns **only EBOOK products** (filter in `ProductCatalogue.all()`).
- **Cart** (`cart/`): single in-memory cart shared by all users. `GET /api/cart`, `POST /api/cart/items` (`{productId, quantity}`), `DELETE /api/cart/items/{id}`, `DELETE /api/cart`.
- **VAT/BTW**: rates per country and category in `VatRates`; every cart line uses **PL** rates. Cart VAT is summed unrounded per line and rounded once (HALF_UP, 2 decimals). Cart returns subtotal, VAT and total.
- **Front-end**: static `src/main/resources/static/index.html`.
- **Not implemented yet**: country selection, shipping, discount codes (`discount.json` exists but is not loaded), quarterly exchange rates, per-market currency.

## Conventions

- Money is `BigDecimal`; round only at the end, HALF_UP to 2 decimals.
- Errors map to `ProblemDetail` in `ApiExceptionHandler` (unknown product → 404, invalid input → 400).

## Requirements

High-level, partly contradictory ("noisy") requirements live in `documents/` (Dutch):

- @documents/implementation-status.md: what is already built per slice, deviations and what is next.
- @documents/slices-checkout.md: the checkout slice plan.
- @documents/requirements.md: derived requirements, assumptions and open points.
- @documents/EuroCart_stakeholder_memos.md: raw stakeholder memos (PO, Finance, Compliance, Sales). Ask before resolving conflicts between them.
