# ParkShare Backend

ParkShare is a Rwanda-first digital parking marketplace and management platform, starting in Kigali.

## Technology Stack

- Java 21
- Spring Boot
- Spring Data JPA and Hibernate
- PostgreSQL
- Flyway database migrations
- Spring Security and JWT authentication
- Maven

## Core Features

- User authentication and role-based access control
- Parking lot, zone, and space management
- Vehicle management
- Reservations and reservation status tracking
- Payments and pricing management
- Parking operations and occupancy tracking
- Check-in and check-out
- Event parking management
- Notifications, administration, and support

## Prerequisites

- JDK 21
- PostgreSQL
- Git

## Configuration

Configure your database connection and JWT secret using local configuration and environment variables.

Never commit real passwords, JWT secrets, or access tokens.

## Run the Application

Quickest way (PostgreSQL + API in Docker):

    cp .env.example .env    # set PARKSHARE_JWT_SECRET and SPRING_DATASOURCE_PASSWORD
    docker compose up --build

Or against your own PostgreSQL, with the variables from `.env.example` exported:

    ./mvnw spring-boot:run

On Windows, run the backend with:

    .\mvnw.cmd spring-boot:run

The application normally runs at http://localhost:8080.

All settings are listed in [.env.example](.env.example). `PARKSHARE_JWT_SECRET` (32+ characters) and the database connection are required.

## Database Migrations

Flyway manages database schema migrations. V17 needs the `btree_gist` extension (it ships with PostgreSQL and can be enabled without superuser rights on PostgreSQL 13+).

## Business Rules (Rwanda)

All amounts are in Rwandan francs (RWF), whole francs only. Times are interpreted in Kigali time (Africa/Kigali, UTC+2). Every value below can be changed with an environment variable.

**Pricing** is always calculated by the server (`GET /api/pricing/quote` shows it before booking):

- Default rate 300 RWF/hour when the operator has not set one, billed per started 30 minutes.
- Daily cap 3,000 RWF per 24 hours; minimum charge 200 RWF; totals rounded to 10 RWF.
- Operators can set HOURLY, DAILY and FLAT rules and special rates (peak, off-peak, weekend, holiday, event), up to 2,000 RWF/hour and 20,000 RWF/day.
- Prices include 18% VAT. The VAT part is shown on quotes, reservations and receipts.
- The ParkShare commission (10%) comes out of the operator's share, not on top of the driver's price.

**Reservations**

- A booking is held for 15 minutes awaiting payment, then expires and frees the space.
- Paid reservations with no check-in 30 minutes after start become NO_SHOW.
- Cancelling 60+ minutes before start gives a full refund, less than 60 minutes gives 50%, and after start gives nothing. If the lot cancels, the driver always gets a full refund.
- Overstay of more than 10 minutes past the end is charged at the lot's normal rate at check-out.
- A space can never be double-booked (application check plus a database constraint).

**Payments**

- MTN MoMo (078/079 numbers) and Airtel Money (072/073), or cash at the gate recorded by staff.
- Provider results arrive at `POST /api/payments/callback/{provider}`, signed with HMAC-SHA256 in `X-ParkShare-Signature`. Callbacks are processed once per event ID.
- The sandbox gateway is the default until MTN/Airtel credentials are added; `POST /api/payments/sandbox/{transactionReference}/complete` simulates the customer approving the prompt.

## Main Flow

1. Operator registers (`accountType: OPERATOR`) and creates a lot. The lot stays PENDING until an admin sets it to ACTIVE.
2. Driver registers with a Rwandan mobile number, adds a vehicle and books: `POST /api/reservations?vehicleId=&parkingLotId=`.
3. Driver pays: `POST /api/payments/reservation/{id}/initiate` with `{"method":"MTN_MOMO"}`.
4. After payment the reservation is CONFIRMED and `GET /api/reservations/{id}/qr` returns the QR token.
5. Attendant scans it: `POST /api/check-ins/scan` (or `/api/check-ins/code` with the reservation code).
6. Exit: `POST /api/check-outs/reservation/{id}` completes the stay and reports any overstay due.

## Tests

    ./mvnw test

Integration tests start a temporary PostgreSQL with Docker (Testcontainers), so Docker must be running.

## Frontend Integration

Web and mobile developers: start with [docs/FRONTEND_INTEGRATION.md](docs/FRONTEND_INTEGRATION.md).

## API Conventions

- Errors return `{"timestamp","status","error","message"}` with 400 (validation), 401, 403, 404 or 409.
- Large lists (`/api/users`, `/api/audit-logs`, `/api/reservations`, `/api/reservations/user/{id}`, `/api/notifications/user/{id}`) are paged: `?page=0&size=20` (max 100), returning `{"content","page","size","totalElements","totalPages","last"}`.

## Project Status

Core booking, payment (sandbox), check-in/out and security flows are implemented and tested.
Still to do before production: real MTN MoMo / Airtel Money adapters, an SMS gateway, RRA EBM invoicing, refresh tokens and password reset.
