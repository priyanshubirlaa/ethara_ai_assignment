# Hotel Booking System

A production-oriented backend REST API built with Spring Boot for managing hotels, rooms, bookings, reviews, authentication, observability, caching, and operational safeguards.

## Overview

This project simulates a hotel booking platform backend with a layered architecture and production-style concerns built in:

- JWT-based authentication and authorization
- Hotel, room, customer, booking, and review management
- Booking validation and overlapping-date protection
- Dynamic pricing API based on occupancy
- AI-powered hotel review summarization
- Redis-backed caching
- Audit logging
- Email notifications
- Rate limiting
- Actuator and Prometheus metrics
- Global exception handling
- Swagger / OpenAPI documentation

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- MySQL
- Redis
- Spring Mail
- Spring Boot Actuator
- Micrometer + Prometheus
- Springdoc OpenAPI / Swagger UI
- Maven

## Architecture

The application follows a standard layered structure:

`controller -> service -> repository -> database`

Main layers:

- Controller layer for REST endpoints
- Service layer for business logic and validations
- Repository layer for JPA queries and persistence
- DTO layer for request/response separation
- Security layer for JWT authentication and role checks
- Exception layer for centralized error responses
- Operational layer for logging, rate limiting, caching, and metrics

## Security

Authentication is handled using JWT tokens. After a successful login, the API returns a token that must be sent in the `Authorization: Bearer <token>` header for protected routes.

Current roles in the project:

- `ADMIN`
- `STAFF`

Access rules currently configured:

- `POST /api/auth/login` is public
- `POST /api/auth/register` requires `ADMIN`
- `/api/hotels/**` requires `ADMIN`
- `/api/hotels/*/rooms/**` requires `ADMIN`
- `/api/customers/**` requires `ADMIN` or `STAFF`
- `/api/bookings/**` requires `ADMIN` or `STAFF`
- `/api/reviews/**` requires `ADMIN` or `STAFF`
- `/swagger-ui/**`, `/v3/**`, `/actuator/**`, `/api/health` are public

## Core Features

### Booking Safety and Validation

- Prevents double booking for overlapping dates
- Rejects bookings in the past
- Ensures `checkOut > checkIn`
- Validates that the selected room belongs to the selected hotel
- Uses optimistic locking on `Booking` with a version field
- Wraps booking flow in transactions for consistency

### Search, Filtering, Sorting, Pagination

Implemented across major modules:

- Hotels
- Rooms
- Bookings

Supported patterns:

- page number
- page size
- sorting by field
- direction `asc` / `desc`
- filtering by city, price range, availability, status, customer, and hotel

### Dynamic Pricing

A separate dynamic pricing API calculates room prices based on hotel occupancy for a requested date range.

Current pricing rules:

- occupancy `>= 80%` -> `basePrice * 1.5`
- occupancy `>= 50%` -> `basePrice * 1.2`
- occupancy `<= 20%` -> `basePrice * 0.8`
- otherwise -> `basePrice * 0.9`

This pricing is currently response-based only and does not permanently overwrite the stored room price.

### Reviews and AI Summary

- Customers/staff can add hotel reviews with rating and comment
- Reviews can be fetched by hotel
- Average rating can be fetched by hotel
- Review comments can be summarized through an AI summary endpoint

### Email Notifications

The system sends email notifications when:

- a booking is created
- a booking is cancelled

The email flow includes hotel and room details plus booking dates.

### Audit Logging

Important actions are written to an audit log table, including:

- hotel added
- room created or updated
- booking created
- booking cancelled
- booking status updates

### Caching

Caching is enabled with Redis and currently used for hotel and room fetch flows.

### Rate Limiting

An in-memory rate limiting filter protects important APIs from abuse.

Protected paths currently include:

- `/api/auth/login`
- `/api/bookings`
- `/api/reviews`

Current default limit:

- `10 requests per minute per IP per endpoint`

### Monitoring and Operations

- Spring Boot Actuator enabled
- Prometheus metrics export enabled
- authentication and booking counters recorded through Micrometer
- custom health support present in the project
- structured logging support included

## Main APIs

All APIs are under `/api`.

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`

### Customer APIs

- `POST /api/customers`
- `POST /api/customers/force`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`

### Hotel APIs

- `POST /api/hotels`
- `GET /api/hotels`
- `GET /api/hotels/{id}`
- `GET /api/hotels/available`

Examples:

```http
GET /api/hotels?city=Delhi&page=0&size=10&sortBy=id&sortDir=asc
GET /api/hotels/available?city=Delhi&checkIn=2026-03-25&checkOut=2026-03-28&page=0&size=10
```

### Room APIs

- `POST /api/hotels/{hotelId}/rooms`
- `GET /api/hotels/{hotelId}/rooms`
- `GET /api/hotels/{hotelId}/rooms/available`
- `GET /api/hotels/{hotelId}/rooms/{roomId}`
- `PATCH /api/hotels/{hotelId}/rooms/{roomId}/price`
- `GET /api/hotels/{hotelId}/rooms/dynamic-pricing`

Examples:

```http
GET /api/hotels/1/rooms?minPrice=1000&maxPrice=5000&page=0&size=10&sortBy=price&sortDir=asc
GET /api/hotels/1/rooms?checkIn=2026-03-25&checkOut=2026-03-28&page=0&size=10
GET /api/hotels/1/rooms/dynamic-pricing?checkIn=2026-03-25&checkOut=2026-03-28&page=0&size=10
```

### Booking APIs

- `POST /api/bookings`
- `GET /api/bookings/{id}`
- `PUT /api/bookings/{id}/cancel`
- `PUT /api/bookings/{id}/confirm`
- `GET /api/bookings`
- `GET /api/bookings/status/{status}`

Examples:

```http
GET /api/bookings?status=CONFIRMED&page=0&size=10&sortBy=id&sortDir=desc
GET /api/bookings?customerId=1&page=0&size=10
GET /api/bookings?hotelId=1&page=0&size=10
```

Allowed booking statuses:

- `CONFIRMED`
- `CANCELLED`

### Review APIs

- `POST /api/reviews`
- `GET /api/reviews/hotel/{hotelId}`
- `GET /api/reviews/hotel/{hotelId}/average`
- `GET /api/reviews/hotel/{hotelId}/summary`

## Example Login Flow

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "password"
}
```

### Use Token

```http
Authorization: Bearer <jwt-token>
```

## Database and Infrastructure

### MySQL

Create the database:

```sql
CREATE DATABASE hotel_booking_system;
```

### Redis

Make sure Redis is running locally on the configured host/port if you want caching enabled.

## Local Configuration

Current `application.properties` includes MySQL, Redis, Actuator, and Prometheus settings. A typical local configuration looks like:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_system
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

spring.data.redis.host=localhost
spring.data.redis.port=6379

management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
```

## Running the Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Default local base URL:

```text
http://localhost:8080
```

## Documentation and Monitoring

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Actuator:

```text
http://localhost:8080/actuator
```

Prometheus metrics:

```text
http://localhost:8080/actuator/prometheus
```

## Highlights

- JWT authentication and authorization
- Role-based API protection
- Customer, hotel, room, booking, and review modules
- Availability-aware hotel and room search
- Dynamic pricing API
- AI review summarization
- Email notifications
- Audit logging
- Redis caching
- Rate limiting
- Actuator and Prometheus integration
- Global exception handling
- Validation, filtering, sorting, and pagination
- Swagger/OpenAPI docs

## Repository

https://github.com/priyanshubirlaa/Hotel_Booking_System

