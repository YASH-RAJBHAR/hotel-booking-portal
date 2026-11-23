# DESIGN.md – Hotel Booking Portal

## 1. Architecture

```
                 ┌─────────────────────────────┐
                 │     Angular 17 SPA           │
                 │  (http://localhost:4200)      │
                 │                              │
                 │  Hotel List  ─►  Hotel Detail │
                 │       │               │       │
                 │  Manage/Cancel ◄──  Voucher   │
                 └───────────┬─────────────────-─┘
                             │  HTTP / JSON (REST)
                             │  Proxy → localhost:8080
                 ┌───────────▼──────────────────┐
                 │   Spring Boot 3 REST API      │
                 │   (http://localhost:8080)     │
                 │                              │
                 │  HotelController              │
                 │  BookingController            │
                 │        │                     │
                 │  HotelService                 │
                 │  BookingService               │
                 │        │                     │
                 │  HotelRepository              │
                 │  BookingRepository            │
                 └───────────┬──────────────────┘
                             │  Spring Data JPA / Hibernate
                 ┌───────────▼──────────────────┐
                 │       MySQL 8                 │
                 │   hotel_booking_db            │
                 │   hotels  |  bookings         │
                 └──────────────────────────────┘
```

**Flow**: The Angular SPA talks exclusively to the Spring Boot REST API over HTTP/JSON. The API validates requests, enforces business rules, and reads/writes data through JPA repositories backed by MySQL. No direct database access from the frontend.

---

## 2. Database Design

### Entities & Relationships

| Table | Key Columns | Notes |
|-------|-------------|-------|
| `hotels` | `id` (PK), `name`, `location`, `price_per_night`, `total_rooms` | One row per hotel. `total_rooms` is the capacity ceiling. |
| `bookings` | `id` (PK), `booking_reference` (UNIQUE), `hotel_id` (FK), `status` (ENUM) | Each row represents one room-booking. |

### Constraints & Indexes

- `bookings.booking_reference` – `UNIQUE` constraint prevents duplicate references.
- Foreign key `bookings.hotel_id → hotels.id` with `ON DELETE RESTRICT`.
- Composite index `idx_booking_hotel_status_dates(hotel_id, status, check_in_date, check_out_date)` enables the overlap COUNT query to run as a covering index scan rather than a full table scan — critical for performance as booking volume grows.

### Assumptions

- Each hotel has a **single room category**. All bookings for a hotel compete for the same `total_rooms` pool.
- Availability is computed dynamically via an overlap query rather than maintained as a separate counter, trading a slightly more complex query for simpler data consistency.
- Soft-cancel (`status = CANCELLED`) is used instead of deletion so the booking history is preserved for audits.

---

## 3. API Design

| Method | Endpoint | Purpose | Status codes |
|--------|----------|---------|-------------|
| GET | `/api/hotels` | List hotels (optionally filtered by `location`, `checkIn`, `checkOut`) | 200 |
| GET | `/api/hotels/{id}` | Hotel details with availability & price preview | 200, 404 |
| GET | `/api/hotels/{id}/alternate-dates` | Smart alternate date suggestions when sold out | 200, 404 |
| POST | `/api/bookings` | Create a booking | 201, 400, 409 |
| GET | `/api/bookings/lookup` | Retrieve booking by reference + email | 200, 404 |
| PATCH | `/api/bookings/{reference}/cancel` | Cancel a confirmed booking | 200, 400, 404 |

### Error Response Format

All errors return a consistent JSON payload:

```json
{
  "timestamp": "2025-01-10T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Check-in date cannot be in the past.",
  "fieldErrors": {
    "checkInDate": "must not be null"
  }
}
```

`fieldErrors` is only populated for bean-validation failures (`@Valid`).

---

## 4. Booking Logic

### Date Validation

1. `checkInDate >= today` – guests cannot book in the past.
2. `checkOutDate > checkInDate` – at least one night is required.

### Overlap Formula (Half-Open Interval)

Two stays **[A_in, A_out)** and **[B_in, B_out)** overlap if and only if:

```
A_in < B_out  AND  A_out > B_in
```

This is the standard half-open interval test. It correctly handles the boundary case: a guest checking out on a date does **not** conflict with a guest checking in on the same date (they share only the boundary, not an actual night).

### Availability Check

```
availableRooms = hotel.totalRooms - COUNT(CONFIRMED bookings overlapping [checkIn, checkOut))
```

If `availableRooms <= 0`, the API returns `409 Conflict`.

### Backend Price Calculation

```java
int nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
BigDecimal total = hotel.getPricePerNight().multiply(BigDecimal.valueOf(nights));
```

The frontend **never** sends a total amount; the backend always recomputes it to prevent tampering.

### Cancellation

Setting `status = CANCELLED` immediately removes that booking from the overlap query, releasing its slot(s) for future bookings. A cancelled booking cannot be cancelled again (returns `400 Bad Request`).

---

## 5. Design Decisions & Trade-offs

### Dynamic Availability vs. Pre-generated Inventory Rows

| Approach | Pros | Cons |
|----------|------|------|
| **Dynamic (chosen)** – count overlapping bookings at query time | Simple schema, no extra maintenance, correct after any cancellation | COUNT query on every request; needs composite index |
| Pre-generate one row per hotel-night | O(1) availability lookup | Complex schema; must backfill on cancellation; prone to drift |

For this scale (5–10 hotels, low concurrency), the dynamic approach is simpler and correct. The composite index makes the query fast.

### Soft Cancel vs. Hard Delete

Soft cancel (`status = CANCELLED`) was chosen because:
- Preserves booking history for audit/support.
- The reference code remains reserved (no risk of reuse).
- Cancellation is instantly reflected in availability without any additional logic.

### Single Room Category per Hotel

Keeping one `total_rooms` counter per hotel keeps the schema and availability logic simple. A multi-category extension would add a `room_types` table and change the overlap query accordingly.

---

## 6. Simultaneous Bookings (Concurrency)

**Problem**: Two users hit `POST /api/bookings` simultaneously for the last available room. Without protection, both pass the availability check before either saves, resulting in overbooking.

**Solution – Pessimistic Write Lock**

```java
// HotelRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT h FROM Hotel h WHERE h.id = :id")
Optional<Hotel> findByIdWithLock(@Param("id") Long id);
```

Inside a `@Transactional` method, the first thread to acquire the lock proceeds with the check and save. The second thread blocks until the first commits. When it resumes, the overlap count now reflects the first booking, and if no room remains it throws `409 Conflict`.

**Why not Optimistic Locking?**

Optimistic locking (`@Version`) is lighter but throws `OptimisticLockException` on conflict, requiring a retry at the application layer. For a booking scenario where a "conflict" means the guest genuinely cannot be served, pessimistic locking gives a cleaner, immediate error without retry complexity.

**Trade-off**: Pessimistic locking reduces throughput under high concurrency for the same hotel. For production scale, one option is to use a distributed lock (e.g., Redis Redlock) or move to an event-driven model, but that is beyond this scope.

---

## 7. Surprise Feature – Smart Alternate Dates & Instant Printable Booking Voucher

We implemented two complementary surprise features designed to elevate guest UX:

### Feature 1: Smart Alternate Dates Suggestion

#### What it does
When a hotel has `0` rooms available for the user's selected date window, the UI prevents dead-ends by displaying a "Try These Available Dates" recommendation banner. It shows up to 3 nearby alternative check-in/check-out windows (e.g., shifted forward or backward by 1–7 days for the exact stay duration) with live room counts and a 1-click button to immediately select the alternate dates.

#### Why this was chosen
Reaching a "Sold Out" state with no guidance is the #1 drop-off cause in travel booking funnels. Flexible leisure travelers (vacations, weekend getaways) are often willing to shift by a day or two if rooms are open.

#### How it is implemented
- **Backend (`GET /api/hotels/{id}/alternate-dates?checkIn=&checkOut=`)**:
  Iterates shift offsets $\pm 1 \dots \pm 7$ days from the requested date range, calculating the overlap count via `BookingRepository.countOverlappingConfirmed()`. Any window with `availableRooms > 0` and `checkInDate >= LocalDate.now()` is collected into `AlternateDateDto` (up to 3 suggestions).
- **Frontend**: The `HotelDetailComponent` detects `availableRooms === 0` and calls `hotelService.getAlternateDates()`. Clicking an alternate dates pill automatically updates the date pickers, triggers dynamic price & availability recalculation, and enables the booking form.

---

### Feature 2: Instant Printable / Downloadable Booking Voucher

#### What it does
Upon booking confirmation (and when retrieving any existing booking via reference + email), the application generates a styled travel voucher card with a prominent **"Print / Save Voucher"** action.

#### Why this was chosen
Travelers need immediate offline proof of reservation (for hotel reception check-in, visa documentation, or expense reporting) without requiring third-party PDF generators or email server dependencies.

#### How it is implemented
- **Clean Voucher Layout**: Displays booking reference code, hotel name & address, guest credentials, stay dates, itemized room calculation, and payment timestamp.
- **`@media print` CSS Styling**: When `window.print()` is triggered, navigation headers, search forms, buttons, and cancel actions are hidden (`display: none !important`), rendering an uncluttered physical/PDF hotel check-in voucher.

---

## 8. Limitations & Future Improvements

| Limitation | Improvement |
|------------|-------------|
| Single room category per hotel | Add a `room_types` table with per-type pricing and inventory |
| No multi-room booking | Allow booking N rooms in one transaction |
| No email notifications | Integrate SendGrid / AWS SES for booking confirmation emails |
| No pagination on hotel list | Add Spring Data `Pageable` and query param `page` / `size` |
| No rate limiting | Add Spring Boot rate-limiting or an API gateway |
| Pessimistic lock reduces throughput | Explore Redis distributed lock or optimistic lock + retry for high traffic |
| Alternate-dates searches sequentially | Could parallelise with `CompletableFuture` |
| No integration / end-to-end tests | Add Testcontainers + an in-memory MySQL for slice tests |
| Frontend not production-built | Add `ng build --configuration production` to CI pipeline |
