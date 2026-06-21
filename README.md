# Movie Ticket Booking System

Backend (REST) for booking movie tickets at scale — seat-level booking with time-bound holds,
tiered pricing, discounts, payment, and refunds. Spring Boot 3.3 / Java 17, PostgreSQL (H2 for
tests).

See [`PLAN.md`](PLAN.md) (architecture) and [`design.md`](design.md) (class model) for the full
design.

## Prerequisites
- Java 17, Maven 3.9+
- PostgreSQL running locally (for the app; tests use in-memory H2)

## Configuration
The app connects to PostgreSQL via env vars (defaults in `src/main/resources/application.yml`):

| Var | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/movieticket` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `APP_JWT_SECRET` | dev default (override in real envs; ≥32 bytes) |
| `APP_JWT_EXPIRATION_MINUTES` | `60` |
| `APP_ADMIN_EMAIL` | `admin@movieticket.local` (seeded admin) |
| `APP_ADMIN_PASSWORD` | `admin12345` |
| `APP_HOLD_TTL_MINUTES` | `5` (seat-hold validity) |
| `APP_HOLD_SWEEP_INTERVAL_MS` | `60000` (expiry sweeper interval) |
| `APP_NOTIFICATION_DISPATCH_INTERVAL_MS` | `10000` (outbox dispatch interval) |
| `APP_NOTIFICATION_REMINDER_INTERVAL_MS` | `300000` (reminder scan interval) |
| `APP_NOTIFICATION_REMINDER_LEAD_MINUTES` | `60` (reminder lead window) |
| `APP_NOTIFICATION_MAX_ATTEMPTS` | `5` (notification retry cap) |

Hibernate manages the schema (`ddl-auto: update`); no migration tool.

## Run
```bash
mvn spring-boot:run                                   # start the app (needs PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=local  # start with local overrides
mvn verify                                            # unit (*Test) + integration (*IT) tests on H2
```

Profiles: base config is `application.yml`; tests use `application-test.yml` (H2). For local
overrides, copy the template (git-ignored personal file):
```bash
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
```
then run with `-Dspring-boot.run.profiles=local`.

## API
Auth (JWT):
- `POST /api/auth/register` — `{email, password}` → 201 `{id, email, role}`. Self-registration
  always creates a `CUSTOMER`.
- `POST /api/auth/login` — `{email, password}` → 200 `{token, tokenType, expiresInMinutes}`.

Authorization (path-based RBAC, `Authorization: Bearer <token>`):
- `/api/admin/**` — ADMIN only.
- `/api/public/**` — browse GETs open; customer write actions require a valid token.

Admin catalog (`/api/admin`, ADMIN): `POST cities`, `POST theaters`, `POST screens` +
`GET screens?theaterId=`, `POST screens/{id}/seats` (bulk layout), `POST movies`,
`POST shows` (publishes the show and generates a priced seat per screen seat),
`POST/GET pricing-tiers`, `POST/GET discount-codes`, `POST/GET refund-policies`.
Public browse (`/api/public`): `GET cities`, `GET theaters?cityId=`, `GET movies`,
`GET shows?cityId=&movieId=&date=` (filters optional; `date` is an ISO UTC day),
`GET shows/{id}/seats` (live seat map: each seat's status and price).
Customer actions (`/api/public`, authenticated): `POST holds` `{showId, seatIds}` (time-bound,
configurable TTL), `DELETE holds/{id}` (release own hold), `POST bookings`
`{holdId, discountCode?, paymentMethod}` (confirm + pay), `GET bookings` (own history),
`GET bookings/{id}` (own booking detail), `POST bookings/{id}/cancel` (refund per policy, releases
seats). Payment uses a mock gateway; `paymentMethod=DECLINE` forces a 402 to exercise the failure
path.

## Notifications
Confirmation and cancellation notifications use a **transactional outbox**: the booking transaction
writes a `PENDING` row (durable, atomic, non-blocking). Delivery happens **off the request thread**
two ways: an `@Async @TransactionalEventListener(AFTER_COMMIT)` dispatches the row **immediately**
after the booking commits (dedicated executor), and a `@Scheduled` dispatcher is the **retry/safety
net** for anything still pending — retrying failures up to a max-attempts cap. A separate scheduled
job enqueues de-duplicated pre-show reminders for confirmed bookings within a lead window. The
sender is a logging stub in place of a real email/SMS provider.

## Concurrency
Seat occupancy lives on a single `ShowSeat` row per (show, seat). Holds/bookings acquire a
pessimistic write lock (`SELECT … FOR UPDATE`, id-ordered) before transitioning status, so
concurrent attempts on the same seat are serialized — exactly one wins, the rest get `409`. No
double-allocation; seats are re-bookable after release/cancellation.

Errors use a consistent body: `{timestamp, status, error, message, path, fieldErrors}`.

## Testing
```bash
mvn verify          # 87 unit (*Test) + 59 integration (*IT) tests on H2
```
Unit tests build objects via factories under `src/test/.../support/factory`; integration tests use
JSON fixtures under `src/test/resources/fixtures` (loaded via `JsonFixtures`). JaCoCo reports are
written to `target/site/jacoco` (unit) and `target/site/jacoco-it` (integration); integration tests
alone cover ~87% of instructions. The concurrency guarantee is exercised by `HoldConcurrencyIT`
(N threads race one seat → exactly one wins).

## Sample end-to-end flow
```bash
# 1. admin logs in (seeded account)
ADMIN=$(curl -s localhost:8088/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@movieticket.local","password":"admin12345"}' | jq -r .token)

# 2. admin seeds catalog: city -> theater -> screen -> seat layout -> movie -> show
curl -s localhost:8088/api/admin/cities    -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' -d '{"name":"Bengaluru"}'
# ...theaters, screens, /screens/{id}/seats, movies, then:
curl -s localhost:8088/api/admin/shows      -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"screenId":1,"movieId":1,"startTime":"2026-07-01T10:00:00Z","showType":"REGULAR","basePrice":200.00}'

# 3. customer registers + logs in
curl -s localhost:8088/api/auth/register -H 'Content-Type: application/json' -d '{"email":"alice@example.com","password":"password1"}'
USER=$(curl -s localhost:8088/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"password1"}' | jq -r .token)

# 4. browse -> seat map -> hold -> book -> history -> cancel
curl -s "localhost:8088/api/public/shows/1/seats"
curl -s localhost:8088/api/public/holds    -H "Authorization: Bearer $USER" -H 'Content-Type: application/json' -d '{"showId":1,"seatIds":[1,2]}'
curl -s localhost:8088/api/public/bookings -H "Authorization: Bearer $USER" -H 'Content-Type: application/json' -d '{"holdId":1,"paymentMethod":"CARD"}'
curl -s localhost:8088/api/public/bookings -H "Authorization: Bearer $USER"
curl -s -X POST localhost:8088/api/public/bookings/1/cancel -H "Authorization: Bearer $USER"
```

## Assumptions
- Schema is generated by Hibernate (`ddl-auto: update`) rather than versioned migrations.
- Timestamps are stored in UTC; money is `BigDecimal` (scale 2), single-currency, no tax modeling.
- Passwords are BCrypt-hashed; emails are normalized to lowercase and unique.
- Self-registration yields `CUSTOMER`; an admin is seeded from `app.admin.*` (override in prod).
- One screen owns one seat layout (defined once); publishing a show freezes a price onto each
  `ShowSeat` (base × pricing-tier multiplier + surcharge; base price if no tier configured).
- A show's `showType` (incl. `WEEKEND`) is admin-set, not derived from the date.
- `DiscountCode.usageLimit == 0` means unlimited; redemption is atomic.
- Payment/refund use a deterministic mock gateway (`paymentMethod=DECLINE` forces failure); no real PSP.
- Refund policy is a set of `hours-before-show → percent` rules; the highest satisfied threshold wins.
- Seat occupancy is the single source of truth on `ShowSeat` (pessimistic lock + `@Version`); no
  global `booking_seat` unique constraint, so seats are re-bookable after cancellation.
