# Wallet Service

A small Spring Boot service that holds user wallets and moves money between them
through a double-entry ledger.

## Quick start

Prerequisites: Java 17+, Maven 3.9+, Docker.

```bash
make start     # Postgres on localhost:5432
make run       # app on http://localhost:8080 (Flyway migrates and seeds on startup)
make test      # full test suite; uses Testcontainers, so Docker must be running
```

If you have an old `challenge_postgres` volume from a previous version, run `make reset-db` first.

## Domain

| Concept | Table | Notes |
|---|---|---|
| User | `users` | |
| Wallet | `wallets` | One per user per currency. `USER` wallets can never go negative. One `SYSTEM` wallet per currency funds deposits. |
| Ledger transaction | `ledger_transactions` | One per money movement. Unique on `(type, idempotency_key)`. |
| Ledger entry | `ledger_entries` | Signed amounts; the entries of a transaction always sum to zero. |

### Money

- Amounts are stored as `BIGINT` **minor units** (cents for USD, yen for JPY). Never floating point.
- The API accepts and returns decimal amounts in major units (e.g. `12.50`). Convert with
  `com.challenge.util.Money`, which knows each currency's decimal places and rejects
  amounts with too much precision instead of rounding.
- Supported currencies: USD, EUR, JPY.

### Moving money

`LedgerService.post(Posting)` is the **only** code path that changes a balance. Every posting is:

- **atomic**: entries and balance updates commit together or not at all
- **idempotent**: reposting the same `(type, idempotencyKey)` returns the original transaction
- **balanced**: legs must sum to zero
- **single-currency**: all wallets in a posting share a currency
- **safe under concurrency**: wallets are locked `FOR UPDATE` in id order, so concurrent postings
  neither lose updates nor deadlock
- **non-overdrawing**: a `USER` wallet's balance never goes below zero (also enforced by a DB check)

`wallets.balance_minor` is a cache of the wallet's ledger entries and must always equal
`SUM(ledger_entries.amount_minor)` for that wallet.

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/users` | |
| GET | `/api/users/{id}` | |
| GET | `/api/users/{id}/wallets` | |
| POST | `/api/users` | `{"name": "...", "email": "..."}` |
| PUT | `/api/users/{id}` | |
| POST | `/api/wallets` | `{"userId": 1, "currency": "USD"}` |
| GET | `/api/wallets/{id}` | |
| GET | `/api/wallets/{id}/entries?limit=50` | Most recent first |
| POST | `/api/wallets/{id}/deposits` | Internal (funding pipeline). Requires `Idempotency-Key` header. `{"amount": 25.00}` |
| GET | `/health` | |

Errors are returned as RFC 7807 problem details (`application/problem+json`).

```bash
curl -X POST localhost:8080/api/wallets/1/deposits \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: dep-123' \
  -d '{"amount": 25.00}'
```

## Project layout

```
src/main/java/com/challenge/
├── controller/   REST endpoints
├── dto/          request/response records
├── entity/       JPA entities
├── exception/    domain exceptions + GlobalExceptionHandler
├── repository/   Spring Data repositories
├── service/      LedgerService, WalletService, UserService
└── util/         Money
src/main/resources/db/
├── migration/    Flyway schema migrations (V1, V2, ...)
└── seed/         local dev data (not loaded by tests)
```

## Conventions

- Schema changes go through a new Flyway migration; never edit an applied one.
  Hibernate runs with `ddl-auto: validate`.
- Controllers speak DTOs, never entities.
- Throw the domain exceptions in `exception/`; `GlobalExceptionHandler` maps them to status codes.
- Anything that moves money goes through `LedgerService`.
- Integration tests extend `IntegrationTest` and run against a real Postgres container.
