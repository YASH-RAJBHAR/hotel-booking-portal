# 🏨 Hotel Booking Portal

A full-stack hotel booking application built with **Angular 17**, **Spring Boot 3**, and **MySQL 8**. This project was built as part of a technical interview assessment.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Database Setup](#database-setup)
4. [Backend Setup](#backend-setup)
5. [Frontend Setup](#frontend-setup)
6. [API Quick-Test Guide](#api-quick-test-guide)
7. [Running Tests](#running-tests)
8. [Project Structure](#project-structure)
9. [Time Spent](#time-spent)
10. [AI Assistance Disclosure](#ai-assistance-disclosure)

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 17+ | Add `JAVA_HOME` to `PATH` |
| Apache Maven | 3.9+ | Or use the included `mvnw` wrapper |
| Node.js | 20+ | |
| npm | 9+ | |
| Angular CLI | 17+ | `npm install -g @angular/cli` |
| MySQL | 8+ | Running locally on port 3306 |

---

## Quick Start

```bash
# 1. Clone the repository
git clone <repo-url>
cd hotel-booking-portal

# 2. Database
mysql -u root -p < database/schema.sql
mysql -u root -p hotel_booking_db < database/data.sql

# 3. Backend (new terminal)
cd backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Edit application.properties with your DB credentials if needed
mvn spring-boot:run

# 4. Frontend (new terminal)
cd frontend
npm install
npm start
```

Open **http://localhost:4200** in your browser.

---

## Database Setup

```bash
# Create database and load schema
mysql -u root -p < database/schema.sql

# Load sample data (6 hotels + 5 sample bookings)
mysql -u root -p hotel_booking_db < database/data.sql
```

### Environment Variables

The backend uses environment variables for credentials. You can set them before running:

```bash
# Windows (PowerShell)
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"

# Linux / macOS
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

Or simply edit `backend/src/main/resources/application.properties` directly (but never commit credentials).

---

## Backend Setup

```bash
cd backend

# Run tests
mvn test

# Start the server
mvn spring-boot:run
```

The API will be available at **http://localhost:8080/api**

---

## Frontend Setup

```bash
cd frontend
npm install
npm start          # development server on http://localhost:4200
npm run build      # production build to dist/
```

---

## API Quick-Test Guide

### List all hotels
```bash
curl http://localhost:8080/api/hotels
```

### Search hotels by location and dates
```bash
curl "http://localhost:8080/api/hotels?location=Mumbai&checkIn=2025-01-10&checkOut=2025-01-13"
```

### Get hotel details with price preview
```bash
curl "http://localhost:8080/api/hotels/1?checkIn=2025-01-10&checkOut=2025-01-13"
```

### Create a booking
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "hotelId": 1,
    "guestName": "John Doe",
    "guestEmail": "john@example.com",
    "checkInDate": "2025-01-10",
    "checkOutDate": "2025-01-13"
  }'
```

### Lookup a booking
```bash
curl "http://localhost:8080/api/bookings/lookup?reference=TC-SAMPLE1&email=priya.sharma@example.com"
```

### Cancel a booking
```bash
curl -X PATCH "http://localhost:8080/api/bookings/TC-SAMPLE1/cancel?email=priya.sharma@example.com"
```

### Get alternate date suggestions (when sold out)
```bash
curl "http://localhost:8080/api/hotels/3/alternate-dates?checkIn=2025-01-10&checkOut=2025-01-12"
```

---

## Running Tests

```bash
cd backend
mvn test
```

Tests cover:
- Valid booking creation and exact total amount calculation
- Date validation (past check-in, check-out before check-in, same date)
- Availability / overlap boundary conditions (sold-out hotel throws 409)
- Cancellation (sets status CANCELLED, rejects duplicate cancellation)

---

## Project Structure

```
hotel-booking-portal/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/example/hotelbooking/
│   │   ├── HotelBookingApplication.java
│   │   ├── config/             # CorsConfig
│   │   ├── controller/         # HotelController, BookingController
│   │   ├── dto/                # Request / Response DTOs
│   │   ├── entity/             # Hotel, Booking JPA entities
│   │   ├── exception/          # Custom exceptions, GlobalExceptionHandler
│   │   ├── repository/         # Spring Data JPA repositories
│   │   └── service/            # HotelService, BookingService
│   └── src/test/java/          # JUnit 5 + Mockito tests
│
├── frontend/                   # Angular 17 SPA
│   └── src/app/
│       ├── components/         # hotel-list, hotel-detail, manage-booking, voucher
│       ├── services/           # hotel.service.ts, booking.service.ts
│       └── models/             # TypeScript interfaces
│
├── database/
│   ├── schema.sql              # DDL — tables, indexes, constraints
│   └── data.sql                # 6 Indian hotels + 5 sample bookings
│
├── README.md                   # This file
└── DESIGN.md                   # Architecture & design write-up
```

---

## Time Spent

Approximately **14 hours** total:
- Database design & seed data: 1 hour
- Backend entities, DTOs, repositories, services, controllers: 5 hours
- Backend unit tests: 1 hour
- Frontend components, routing, forms, UI: 5 hours
- Documentation (README, DESIGN.md): 2 hours

---

## AI Assistance Disclosure

This project was developed with significant assistance from an AI coding assistant (Google Antigravity / Gemini). All generated code has been reviewed for correctness, and the author can explain every design decision and code path during the interview. Libraries, frameworks, and documentation consulted include Spring Boot docs, Angular docs, and MySQL 8 reference.
