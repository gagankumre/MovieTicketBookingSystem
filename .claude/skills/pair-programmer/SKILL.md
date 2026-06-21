---
name: pair-programmer
description: Pair-programming guide for building the Movie Ticket Booking System (Spring Boot 3.3 / Java 17, Postgres + H2, JWT). Use when writing, reviewing, or extending code in this project — it carries the conventions, architecture, and workflow rules to follow. Invoke for any implementation work on this repo.
---

# Pair Programmer — Movie Ticket Booking System

You are pair-programming on this project. Follow the conventions, architecture, and workflow
below for every change. The authoritative design lives in `PLAN.md` (architecture) and
`design.md` (class sketch) at the repo root — consult them before adding code.

## How we work together — Test-Driven Development (TDD)

We practice TDD with a **ping-pong pairing workflow**. The cycle is non-negotiable for any
behavioral change:

1. **Red** — write a failing test that pins the next small piece of behavior. Run it; confirm it
   fails for the right reason.
2. **Green** — write the minimum production code to make that test pass. Run it; confirm green.
3. **Refactor** — clean up production and test code while keeping the suite green.

**Ping-pong rule:** alternate authorship between the pair. When I write a failing test, the next
step is the implementation that satisfies it; after it's green, the next failing test is written
before any further production code. Never write production code without a failing test demanding
it, and never write more test than is needed to fail.

**Automate the loop each cycle:**
- **Generate tests** up front for the behavior being added — don't backfill them after the code.
- **Track coverage** after each green step (`mvn test` runs JaCoCo; check the report under
  `target/site/jacoco/`). Call out any new production code that dropped line/branch coverage and
  add tests to cover it before moving on.
- Keep steps small — one behavior per red/green cycle.

## Testing rules

- **Unit tests** — write them for the **correctness of important methods**, i.e. anything with
  real logic: pricing math (`PricingService`), discount validation/redeem (`DiscountService`),
  refund-policy boundaries (`RefundService`), hold-expiry checks, seat status CAS transitions, and
  JWT generation/validation. Pure logic → fast unit tests with mocks, no Spring context.
- **Integration tests** — **whenever an API endpoint is created or updated, add or update its
  integration test** in the same change. Use `@SpringBootTest` + MockMvc against the H2 test
  profile. Cover the happy path, validation/error responses, RBAC (403/401), and the
  concurrency/expiry behavior where the endpoint touches seats.
- A change that adds/changes an endpoint is **not done** until its integration test exists and
  passes. A change that adds/changes an important method is **not done** until its unit test
  exists and passes.
- **Unit tests build objects via test factories, not raw constructors.** Keep per-entity factories
  under `src/test/java/.../support/factory/` (e.g. `ShowSeatFactory`, `UserFactory`) with
  **overloaded** creator methods + sensible defaults. **Reuse** them across tests; add new
  overloads **only as a test needs them**, and **actively refactor/consolidate** to prevent a
  proliferation of near-duplicate methods (collapse overlapping overloads, prefer defaults +
  a few meaningful variants over one-off methods).
- **Integration tests use JSON fixtures, not inline JSON.** Keep request and expected-response
  bodies in files under `src/test/resources/fixtures/<feature>/request/*.json` and
  `.../response/*.json`, loaded via the `JsonFixtures` test helper. Send the request fixture as the
  body; assert the response against the response fixture with a lenient JSON comparison that
  ignores volatile fields (ids, tokens, timestamps). Don't embed JSON string literals in test
  code — add or reuse a fixture file instead.

## Source of truth — PLAN.md and design.md

`PLAN.md` (architecture) and `design.md` (class sketch) at the repo root are the **authoritative
specification**. All code we write must conform to them.

- **Read them first.** Before implementing anything, consult both files and follow the entities,
  APIs, enums, package layout, and concurrency strategy they define.
- **Stay in scope.** If a requested change or an implementation detail would deviate from these
  files — a new entity/field/enum, a different API shape, a changed locking or pricing approach,
  removing something they specify — **do not silently implement it.** Stop and **discuss the
  deviation first**: explain what's out of scope, why it's needed, and the options.
- **Update only on approval.** If the user decides to go ahead with the deviation, **first update
  `PLAN.md` and/or `design.md`** to reflect the new design, then implement against the updated
  files. The spec leads the code, never the other way around.
- Keep `PLAN.md` and `design.md` mutually consistent — a change to one that affects the other must
  update both.

**They are living documents, not gospel.** `PLAN.md` and `design.md` are the working spec, but
they are **not fully accurate and may contain inconsistencies, gaps, or sub-optimal choices.** Treat
them as something to improve, not just obey:

- **Reiterate after every change or design decision.** Each time we add code or make a design call,
  re-read the relevant parts of both files and actively look for issues — contradictions between the
  two, stale entries, things the new decision invalidated, missing pieces, or weaker designs than
  what we just learned. Surface what you find.
- **Iterate to improve them.** When you spot a problem or a better approach, propose it, and (on
  agreement) update the files so they get *more* correct over time. The docs should converge toward
  the best design for the problem statement, not stay frozen.
- **The one hard limit is scope.** Every change — to code or to the docs — must stay **within the
  scope of the problem statement** (the movie-ticket-booking PRD). Improve freely inside that
  boundary; never expand beyond it. If something seems to need going out of scope, stop and raise it
  rather than drifting.

## Code quality & design principles

- **OOP + clean code.** Follow standard object-oriented design (encapsulation, single
  responsibility, clear interfaces, favour composition) and clean-code practices: intention-
  revealing names, small focused methods, no duplication, no dead code, minimal surface area.
- **No unnecessary comments.** Let clear names and small methods carry the meaning. Comment only
  what the code can't say itself — non-obvious *why*, tricky invariants, the concurrency rationale.
  No narration of obvious lines, no commented-out code, no redundant Javadoc that restates the
  signature.
- **Always look at the bigger picture.** Before writing or changing code, actively review the
  existing code and understand its relationships with the rest of the system — who calls it, what
  it depends on, what depends on it. Never edit a piece in isolation.
- **Flag redundancy and suggest refactors.** If a new piece of code makes something else redundant
  (duplicates logic, supersedes a method, overlaps an existing abstraction), **flag it explicitly**
  and propose the refactor rather than leaving both. Trace all usages and relationships of the
  affected code *before* refactoring so nothing breaks silently.

## Work in small, complete, commit-sized pieces

- Break every larger task into **smaller complete flows** — each a self-contained, working,
  tested slice that could stand on its own.
- Each piece must be a **clean git commit checkpoint**: it compiles, its tests pass, and it
  represents one coherent unit of work (not a half-finished change).
- **Before starting, always ask the user how much to cover in one go** — confirm the scope/size
  of the next slice before writing code, and stop at that boundary.

**Build by priority, and create supporting pieces on demand.**
- Sequence the work by the **importance and priority of each component** — build the
  highest-value user flows first (e.g. auth, then the core booking path), not bottom-up by layer.
- **Don't build infrastructure ahead of need.** Cross-cutting/supporting pieces — repositories,
  the global exception handler, mappers, config classes — are added **when a feature actually
  needs them**, as part of that feature's slice, not as separate upfront phases. Add exactly the
  repository methods / exception types the current slice requires, and grow them later.

## Protect existing behavior (no regressions)

- **New code must not alter existing flows, change their functionality, or introduce bugs.**
  Additions are additive — existing callers and behavior stay intact unless a change is explicitly
  agreed.
- **Guard with tests beforehand.** Before adding code that touches or sits near existing
  functionality, make sure that functionality is covered by tests *first* (write characterization
  tests if coverage is missing). Those tests are the safety net that proves the existing flow still
  behaves identically after the change.
- The full suite must stay green after every addition. If a pre-existing test must change, that is
  a behavior change — stop and discuss it before proceeding (see Source of truth).

## Architecture & conventions

**Layering & structure**
- Package **by layer**: `web/`, `service/`, `repository/`, `domain/`, `security/`, `config/`,
  `notification/`, `scheduler/` under `com.example.movieticket`.
- Strict layering: `controller → service → repository`. Controllers never touch repositories or
  entities directly.
- **Controllers contain no business logic** — they bind/validate input, call a service, map the
  result. All business rules live in services.
- **Entities never leave the service layer.** Map entity ↔ DTO before returning.

**DTOs & mapping**
- Request/response DTOs are **classes** (Lombok `@Value`/`@Builder` for responses, validated
  request DTOs as needed).
- Entity ↔ DTO mapping via **MapStruct** (compile-time generated mappers in `web/` or a `mapper/`
  package).

**API design**
- REST URLs use **plural nouns** under `/api` (e.g. `/api/shows/{id}/seats`); HTTP verbs convey
  action. **No URL versioning** (`/v1`) for this project.
- **List/browse endpoints accept Spring `Pageable`** (page/size/sort) and return paged results.
- Standard error body from the `@RestControllerAdvice`:
  `{ timestamp, status, error, message, path, fieldErrors }` — `fieldErrors` is a field→message
  map for validation failures. Status mapping per `PLAN.md` (409 seat/hold conflict, 402 payment,
  404 not-found, 400 validation, 401/403 auth).

**Configuration**
- **Externalize every config value** — no magic numbers/strings hardcoded for things like hold TTL,
  sweeper interval, JWT secret/expiry, refund defaults, pricing knobs.
- **Every time a new config is added, add the variable and its value to the base config file**
  (`application.yml`). Bind it via `@ConfigurationProperties`/`@Value`, give it a sensible default,
  and keep `application.yml` the single place all tunables are declared.
- Profiles: `application.yml` = base (Postgres defaults); `application-test.yml` = H2 for automated
  tests; `application-local.properties` = editable per-developer local overrides (`local` profile,
  **git-ignored**) — the committed template is `application-local.properties.example`. When a new
  tunable matters for local runs or tests, mirror it into the test profile and the
  `.example` template (never edit a developer's ignored local file).

**Persistence**
- IDs: `GenerationType.IDENTITY`.
- Money: `BigDecimal`, scale 2 — never `double`/`float`.
- Timestamps: store as **UTC `Instant`**, convert at the edges (drives hold-expiry / refund
  windows). *(Supersedes the `LocalDateTime` shown in `design.md`/`PLAN.md`.)*
- Enums: `@Enumerated(EnumType.STRING)` everywhere — never ordinal.

**Transactions & seat locking**
- `@Transactional` lives **only on service methods** — never controllers or repositories.
- Query/browse service methods are `@Transactional(readOnly = true)`.
- **All seat status transitions** (`AVAILABLE`/`HELD`/`BOOKED`) go through the single
  `SeatLockManager` using `@Lock(PESSIMISTIC_WRITE)` — no ad-hoc seat-status writes anywhere else.
- When locking multiple seats, **lock in deterministic id order** to prevent deadlocks.

**Exception handling**
- Use **global exception handling** (one `@RestControllerAdvice`) and throw our **own custom,
  meaningful domain exceptions** — never raw `RuntimeException`/`IllegalArgumentException` for
  domain failures, and no try/catch-to-response in controllers. Services throw; the advice maps to
  HTTP status. The exception hierarchy and status mapping are specified in `PLAN.md` — follow it.

**Validation, security, logging**
- Bean Validation (`@Valid`) on request DTOs **at the controller boundary**; services assume the
  request shape is valid (no scattered manual null-checks for input shape).
- RBAC: `@PreAuthorize` on endpoints for role checks; **resource-ownership checks in the service
  layer** (e.g. this booking belongs to this user).
- Inject via **constructor injection only** (no field `@Autowired`); Lombok used freely
  (`@RequiredArgsConstructor`, etc.). Be careful with bidirectional entity relationships under
  `@Data`/`@ToString` (avoid recursion / lazy-loading traps).
- Log via **SLF4J** (`@Slf4j`); add **rich logging wherever it adds value** — entry/exit of
  meaningful flows (hold, book, cancel, payment, refund), seat-status transitions, expiry sweeps,
  async notification dispatch, and all caught/translated exceptions. Use parameterized logging
  (`log.info("...{}...", id)`), never string concatenation. Levels: INFO for flow milestones,
  DEBUG for detail, WARN for recoverable issues, ERROR for failures. **Never log** passwords,
  JWTs, or full payment data.

**Test naming**
- Unit tests: `*Test` (run by Surefire). Integration tests: `*IT` (run by Failsafe). Keeps fast
  unit tests separate from Spring-context integration tests.

## Keep the README current

- After adding code, **update `README.md` if the change warrants it** — new setup step, new
  env/config, new run command, a meaningful new feature/API area, or a new documented assumption.
- **Keep the README short.** It is a quick-start + map, not exhaustive docs: what the project is,
  how to run it and the tests, key assumptions, and a pointer to `PLAN.md`/`design.md` for detail.
  Don't duplicate the full design or list every endpoint — link to the spec instead.

## Things to always do
- Follow the Red → Green → Refactor cycle; commit only with the suite green.
- Add the integration test alongside any new/changed API endpoint.
- Add a unit test for any new/changed method that carries logic.
- Re-run `mvn test` and check coverage before declaring a step complete.

## Things to never do
- Never write production code without a failing test that requires it.
- Never add or change an endpoint without its integration test in the same change.
- Never lower coverage on new logic without flagging it and adding tests.
