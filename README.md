# Book Keeping API

## Overview
Book Keeping API is a Spring Boot service for managing finance operations across reimbursements, budgeting, approvals, accounting, and audit logging.

## Modules

- reimbursement = request submission
- accounting = ledger posting
- budget = category allocation
- audit = immutable logs
- approval = workflows
- receipt = receipt capture and attachment
- user = user profiles and access
- shared = shared utilities and cross-cutting concerns

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- PostgreSQL
- Spring Data JPA

## Project Structure

- `src/main/java/com/calvary/finance/` root package
- Sub-packages per module (see Modules)

## Setup

### Prerequisites
- Java 17+
- PostgreSQL running locally or accessible remotely

### Environment Variables (recommended)
Set these in your shell or deployment environment:

- `DB_URL` (example: `jdbc:postgresql://localhost:5432/book_keeping`)
- `DB_USER`
- `DB_PASSWORD`

### Local Development (no secrets in repo)
1. Copy the example file to a local, ignored file:
   - `src/main/resources/application-example.properties`
   - `src/main/resources/application-local.properties`
2. Run with the local profile:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## Build and Run

```bash
export DB_URL=jdbc:postgresql://localhost:5432/book_keeping
export DB_USER=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run
```

## Endpoints

### Budget Categories

- `GET /api/v1/book-keeping/budget/categories` — list all categories
- `GET /api/v1/book-keeping/budget/categories/active` — list active categories
- `PUT /api/v1/book-keeping/budget/categories/{accNo}/change-status` — toggle active status
- `POST /api/v1/book-keeping/budget/categories` — create a category

### Reimbursements

- `POST /api/v1/book-keeping/reimbursement/create` — create reimbursement request

## Validation Errors

Validation errors return a 400 with field details:

```json
{
  "timestamp": "2026-03-17T06:20:29.667Z",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/v1/book-keeping/budget/categories",
  "fieldErrors": [
    { "field": "accNo", "message": "accNo already exists" }
  ]
}
```

## Test

```bash
./mvnw test
```

## Notes
- Module packages are currently placeholders; add services, controllers, and entities within each module as needed.
