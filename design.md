# Movie Ticket Booking System — Design

Class / interface sketch (fields prefixed `-`, methods prefixed `+`). The seat-hold concurrency
core mirrors the inventory block/confirm pattern: a seat is *blocked* (held) for an order, then
*confirmed* on payment, with compare-and-set status transitions and time-bound expiry.

---

## Core booking & concurrency

### BookingService
 - holdRepo: SeatHoldRepository
 - showSeatRepo: ShowSeatRepository
 - bookingRepo: BookingRepository
 - pricingService: PricingService
 - discountService: DiscountService
 - paymentGateway: PaymentGateway
 - refundService: RefundService
 - notifier: NotificationService
 - holdTtl: Duration

    + holdSeats(userId, showId, seatIds: List<int>) -> SeatHold        // locks rows, blocks seats
    + releaseHold(holdId, userId) -> boolean
    + confirmBooking(holdId, userId, discountCode, paymentMethod) -> Booking
    + cancelBooking(bookingId, userId) -> Refund
    + getBookingHistory(userId) -> List<Booking>

### SeatLockManager
 - showSeatRepo: ShowSeatRepository

    + blockSeats(showId, seatIds, holdId) -> boolean    // SELECT ... FOR UPDATE; AVAILABLE -> HELD
    + confirmSeats(holdId) -> boolean                   // HELD -> BOOKED
    + releaseSeats(holdId) -> boolean                   // HELD -> AVAILABLE

### HoldSweeperJob
 - holdRepo: SeatHoldRepository
 - lockManager: SeatLockManager

    + sweepExpiredHolds() -> void          // @Scheduled: HELD seats of expired holds -> AVAILABLE

---

## Domain entities

### class: ShowSeat   (single source of truth — one row per show+seat)
 - id: long
 - show: Show
 - seat: Seat
 - status: SeatStatus
 - price: BigDecimal
 - currentHoldId: Long                       // set while HELD, else null
 - currentBookingId: Long                     // set while BOOKED, else null
 - version: long                              // @Version optimistic backstop

    + ShowSeat(show, seat, price)
    + block(holdId) -> boolean               // AVAILABLE -> HELD, sets currentHoldId
    + confirm(bookingId) -> boolean          // HELD -> BOOKED, clears hold, sets currentBookingId
    + release() -> boolean                   // HELD|BOOKED -> AVAILABLE, clears both pointers
    + casStatus(prev, next) -> boolean

 // No double-allocation: exactly one ShowSeat row per (show, seat); transitions run under a
 // PESSIMISTIC_WRITE lock with @Version as the lost-update backstop. Re-bookable after cancel.

### class: SeatHold
 - id: long
 - user: User
 - show: Show
 - status: HoldStatus
 - expiresAt: Instant

    + SeatHold(user, show, ttl)
    + isExpired(now) -> boolean
    + casStatus(prev, next) -> boolean

 // Covered seats found via ShowSeat.currentHoldId while the hold is ACTIVE (no join table).

### class: Booking
 - id: long
 - user: User
 - show: Show
 - status: BookingStatus
 - subtotal: BigDecimal
 - discountAmount: BigDecimal
 - totalAmount: BigDecimal
 - discountCode: String
 - createdAt: Instant
 - version: long                              // @Version — guards against double-cancel/refund

    + Booking(user, show, subtotal, discountAmount, totalAmount)
    + casStatus(prev, next) -> boolean

### class: BookingSeat   (historical line item — one per seat per booking)
 - id: long
 - booking: Booking
 - showSeat: ShowSeat
 - pricePaid: BigDecimal                      // price snapshot at booking time

    + BookingSeat(booking, showSeat, pricePaid)

 // unique (booking, showSeat). No global unique on showSeat — a cancelled+rebooked seat
 // legitimately yields a new row, preserving history.

### class: Payment
 - id: long
 - booking: Booking                           // many payments per booking (attempts, refunds)
 - status: PaymentStatus
 - amount: BigDecimal
 - method: String
 - gatewayRef: String

    + Payment(booking, amount, method)
    + markSuccess(gatewayRef)
    + markRefunded()

### class: Refund
 - id: int
 - bookingId: int
 - amount: BigDecimal
 - status: PaymentStatus
 - processedAt: Instant

    + Refund(bookingId, amount)

---

## Catalog entities

### class: City
 - id: int
 - name: String

### class: Theater
 - id: int
 - cityId: int
 - name: String
 - address: String

### class: Screen
 - id: int
 - theaterId: int
 - name: String

### class: Seat   (physical layout)
 - id: int
 - screenId: int
 - rowLabel: String
 - seatNumber: int
 - category: SeatCategory

### class: Movie
 - id: int
 - title: String
 - language: String
 - durationMinutes: int
 - certification: String

### class: Show
 - id: int
 - screenId: int
 - movieId: int
 - startTime: Instant
 - endTime: Instant
 - showType: ShowType
 - basePrice: BigDecimal

    + Show(screenId, movieId, startTime, showType, basePrice)
    + publish() -> List<ShowSeat>          // generates a ShowSeat per seat with resolved price

---

## Pricing, discounts, refunds

### PricingService
 - pricingTierRepo: PricingTierRepository

    + resolvePrice(basePrice, category: SeatCategory, showType: ShowType) -> BigDecimal

### class: PricingTier
 - id: int
 - category: SeatCategory
 - showType: ShowType
 - multiplier: BigDecimal
 - surcharge: BigDecimal

### DiscountService
 - discountRepo: DiscountCodeRepository

    + apply(code: String, subtotal: BigDecimal) -> BigDecimal      // validates + returns discount
    + redeem(code: String) -> boolean                              // atomically increments usedCount

### class: DiscountCode
 - id: int
 - code: String
 - type: DiscountType
 - value: BigDecimal
 - maxDiscount: BigDecimal
 - minBookingAmount: BigDecimal
 - validFrom: Instant
 - validTo: Instant
 - usageLimit: int                            // 0 = unlimited
 - usedCount: int
 - active: boolean
 - version: long                              // @Version — guards usageLimit under concurrent redeem

    + isValid(now, subtotal) -> boolean
    + computeDiscount(subtotal) -> BigDecimal

### RefundService
 - refundPolicyRepo: RefundPolicyRepository

    + computeRefund(booking, showStart, now) -> BigDecimal

### class: RefundPolicy
 - id: int
 - hoursBeforeShow: int          // applies when (showStart - now) >= hoursBeforeShow
 - refundPercent: int

---

## Auth & RBAC

### class: User
 - id: int
 - email: String
 - passwordHash: String
 - role: Role

### AuthService
 - userRepo: UserRepository
 - jwtService: JwtService
 - passwordEncoder: PasswordEncoder

    + register(email, password, role) -> User
    + login(email, password) -> String          // returns JWT

### JwtService
    + generateToken(user) -> String
    + parse(token) -> Claims
    + isValid(token) -> boolean

---

## Notifications (non-blocking)

### NotificationService
 - outboxRepo: NotificationOutboxRepository

    + onBookingConfirmed(booking) -> void        // @Async event listener -> outbox + stub send
    + onBookingCancelled(booking, refund) -> void

### class: NotificationOutbox
 - id: int
 - type: String
 - recipient: String
 - payload: String
 - status: String
 - attempts: int

---

## Enums

### enum: SeatStatus
AVAILABLE,
HELD,
BOOKED

### enum: HoldStatus
ACTIVE,
CONVERTED,
EXPIRED,
RELEASED

### enum: BookingStatus
CONFIRMED,
CANCELLED

### enum: PaymentStatus
SUCCESS,
FAILED,
REFUNDED

### enum: SeatCategory
REGULAR,
PREMIUM

### enum: ShowType
REGULAR,
WEEKEND

### enum: DiscountType
PERCENT,
FLAT

### enum: Role
ADMIN,
CUSTOMER
