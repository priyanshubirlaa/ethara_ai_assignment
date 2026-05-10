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

## Backend Highlights

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

- ## Frontend

The project also includes a modern React-based frontend for interacting with the Hotel Booking System backend APIs. The frontend provides a dashboard-driven experience for authentication, hotel search, room browsing, customer management, booking creation, and booking lifecycle management.

The frontend is built with React + Vite and communicates with the Spring Boot backend through REST APIs using a centralized API layer.

### Frontend Tech Stack

- React 18
- Vite
- JavaScript (ES6+)
- Tailwind CSS
- Lucide React Icons
- Fetch API
- Local Storage
- React Hooks

### Frontend Architecture

The frontend follows a component-based architecture using React functional components and hooks.

Main structure:

`UI Components -> API Service Layer -> Spring Boot REST APIs`

The frontend consists of:

- Reusable UI components for forms, buttons, metrics, and empty states
- Centralized API request handling
- JWT-based authentication flow
- Local storage-based session persistence
- Dynamic dashboard rendering
- State management using React hooks
- Backend communication through REST APIs

### Frontend Features

### Authentication and Authorization

The frontend supports:

- Login using email and password
- Signup with role selection
- JWT token storage in browser local storage
- Automatic authentication header injection
- Role-based UI rendering
- Logout functionality
- Session persistence across refresh

JWT token is stored locally:

```text
hotel_token
```

Current role storage:

```text
hotel_role
```

Authorization header format:

```http
Authorization: Bearer <jwt-token>
```

### API Integration Layer

A centralized API utility is implemented to standardize backend communication.

Features of the API layer include:

- Automatic `Content-Type: application/json`
- JWT token injection for protected APIs
- Public API handling through `auth: false`
- Centralized API error handling
- Response parsing for JSON and text responses
- Standardized exception handling
- Field-level validation error formatting
- Pagination helper methods

The API utility handles:

- Success responses
- Validation failures
- HTTP status-based errors
- Backend connectivity issues
- Unauthorized session handling

Example API request flow:

```javascript
apiRequest("/auth/login", {
  method: "POST",
  body: JSON.stringify(login),
  auth: false,
});
```

### Dashboard Features

The frontend dashboard provides:

#### Authentication Dashboard

- Login screen
- Signup screen
- Role selection (`ADMIN`, `STAFF`)
- Error and success notification handling

#### Hotel Search

Allows users to:

- Search hotels by city
- Filter hotels by check-in date
- Filter hotels by check-out date
- View hotel availability
- Display hotel cards dynamically

Example search:

```http
GET /api/hotels/available?city=Delhi&checkIn=2026-03-25&checkOut=2026-03-28&page=0&size=12
```

#### Room Search and Filtering

Users can:

- View rooms for selected hotels
- Filter rooms by price range
- Filter by check-in/check-out dates
- Sort rooms by price
- Select rooms directly for booking

Room filtering example:

```http
GET /api/hotels/{hotelId}/rooms?minPrice=1000&maxPrice=5000&page=0&size=10&sortBy=price&sortDir=asc
```

#### Customer Management

The dashboard supports customer creation.

Features include:

- Create customer
- Store selected customer
- Auto-fill customer ID during booking
- Customer confirmation display

Customer fields:

- Name
- Email
- Phone number

#### Booking Management

Users can:

- Create bookings
- Select customer, hotel, and room
- Choose check-in/check-out dates
- View selected room details
- Load booking history
- Filter bookings
- Confirm bookings
- Cancel bookings

Booking filters include:

- booking status
- customer ID
- hotel ID

Supported booking actions:

- `CONFIRM`
- `CANCEL`

Supported booking statuses:

- `CONFIRMED`
- `CANCELLED`

### State Management

React hooks are used for frontend state management.

Main hooks used:

- `useState`
- `useEffect`
- `useMemo`

The application manages state for:

- Authentication
- Hotels
- Rooms
- Customers
- Bookings
- Filters
- Notifications
- Error handling
- Loading states

### UI and Styling

The frontend uses Tailwind CSS for styling and responsive layouts.

UI capabilities include:

- Responsive dashboard layout
- Modern hotel booking interface
- Cards and metrics
- Loading indicators
- Dynamic notifications
- Form validation visuals
- Error handling UI
- Success messages
- Responsive grids
- Search and filter forms

Icons are implemented using `Lucide React`.

Current UI sections include:

- Authentication panel
- Customer management panel
- Booking management panel
- Hotel search section
- Room selection section
- Booking dashboard

### Error Handling

The frontend implements centralized error handling.

Current error handling includes:

- API failure handling
- Validation message rendering
- Backend unavailable detection
- Unauthorized session logout
- HTTP status display
- Field-level error rendering

Examples:

- Invalid credentials
- Validation errors
- Backend server unavailable
- Unauthorized access
- Network failure

### Local Development Setup

Install dependencies:

```bash
npm install
```

Start frontend:

```bash
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

### Vite Proxy Configuration

The frontend uses Vite proxy configuration to communicate with the Spring Boot backend.

Current proxy:

```javascript
server: {
  port: 5173,
  proxy: {
    "/api": {
      target: "https://localhost:8080",
      changeOrigin: true,
      secure: false
    }
  }
}
```

This allows frontend API calls such as:

```text
/api/auth/login
```

to automatically proxy to:

```text
https://localhost:8080/api/auth/login
```

### Frontend Project Structure

Example structure:

```text
src/
│── api.js
│── App.jsx
│── main.jsx
│── styles.css
│
public/
│
index.html
vite.config.js
```

### Frontend Highlights

- React + Vite architecture
- JWT authentication flow
- Role-based UI rendering
- API proxy integration
- Centralized API request utility
- Customer creation workflow
- Hotel availability search
- Room filtering and selection
- Booking creation and management
- Booking confirmation and cancellation
- Error handling and validation
- Tailwind responsive UI
- Lucide React icons
- Persistent login using local storage
- Dynamic dashboard rendering
- Pagination support
- Search, filtering, and sorting

<img width="1920" height="923" alt="image" src="https://github.com/user-attachments/assets/5c777265-aad0-4325-b8f7-2cd87da3f1a4" />



