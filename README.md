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

On Windows, run the backend with:

    .\mvnw.cmd spring-boot:run

The application normally runs at http://localhost:8080.

## Database Migrations

Flyway manages database schema migrations.

## Project Status

Under active development.
