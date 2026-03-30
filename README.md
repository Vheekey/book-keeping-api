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

```text
src/main/java/com/calvary/finance/
├── budget/category        # budget category API, validation, persistence
├── reimbursement          # reimbursement API, workflow, persistence
├── audit                  # audit log persistence/services
└── shared                 # exception handling and shared config
```

## Prerequisites

- Java 17+
- PostgreSQL

## Configuration

The application reads datasource settings from `src/main/resources/application.properties`.

Supported environment variables:

- `DB_URL` default: `jdbc:postgresql://localhost:5432/book_keeping`
- `DB_USER` default: ``
- `DB_PASSWORD` default: ``

For local development, copy the example config and override values as needed:

```bash
cp src/main/resources/application-example.properties src/main/resources/application-local.properties
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Notes:

- Hibernate DDL auto is disabled: `spring.jpa.hibernate.ddl-auto=none`
- Flyway auto-run is disabled: `spring.flyway.enabled=false`
- CORS currently allows `http://localhost:5173` and `http://127.0.0.1:5173`

## Run the Application

```bash
export DB_URL=jdbc:postgresql://localhost:5432/book_keeping
export DB_USER=postgres
export DB_PASSWORD=postgres

./mvnw spring-boot:run
```

## Database Migrations

Flyway migrations are present under `src/main/resources/db/migration` and are intended to be run manually:

```bash
./mvnw \
  -Dflyway.url=jdbc:postgresql://localhost:5432/book_keeping \
  -Dflyway.user=postgres \
  -Dflyway.password=postgres \
  flyway:migrate
```

Available migration files:

- `V1__create_budget_categories_table.sql`
- `V2__create_reimbursements_table.sql`
- `V3__create_audit_logs_table.sql`

Manual rollback scripts are stored in `src/main/resources/db/rollback`.

Seed data for budget categories is available in `src/main/resources/db/seeders/budget_categories.sql`.

## API Base Path

All endpoints are served under:

```text
/api/v1/book-keeping
```

## Endpoints

### Budget Categories

#### `GET /api/v1/book-keeping/budget/categories`

Returns all budget categories.

#### `GET /api/v1/book-keeping/budget/categories/active`

Returns only active budget categories.

#### `POST /api/v1/book-keeping/budget/categories`

Creates a budget category.

Request body:

```json
{
  "accNo": "1001",
  "description": "Transport"
}
```

#### `PUT /api/v1/book-keeping/budget/categories/{accNo}/change-status`

Toggles the active status for the category identified by `accNo`.

### Reimbursements

#### `POST /api/v1/book-keeping/reimbursements/create`

Creates a reimbursement request.

Request body:

```json
{
  "expenditureDate": "2024-06-01",
  "name": "John Doe",
  "description": "Fuel reimbursement",
  "amount": 1500.00,
  "shouldReimburse": true,
  "accountName": "John Doe",
  "clearingNumber": "1234",
  "accountNumber": "1234567890",
  "accNo": "1001",
  "phoneNumber": "+233123456789",
  "isCorrect": true
}
```

#### `GET /api/v1/book-keeping/reimbursements`

Returns reimbursements with optional filtering and pagination.

Query parameters:

- `pageNumber` default: `0`
- `pageSize` default: `10`
- `status` default: `all`
- `startDate` optional, ISO date
- `endDate` optional, ISO date

Supported reimbursement statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`
- `PAID`

#### `GET /api/v1/book-keeping/reimbursements/{reimbursementId}`

Returns a single reimbursement by ID.

#### `POST /api/v1/book-keeping/reimbursements/{reimbursementId}/approve`

Approves or rejects a reimbursement.

Request body:

```json
{
  "comment": "Approved for payout",
  "isApproved": true
}
```

#### `POST /api/v1/book-keeping/reimbursements/{reimbursementId}/payout`

Marks an approved reimbursement as paid.

## Error Responses

Validation and runtime failures are returned as API errors in this format:

```json
{
  "timestamp": "2026-03-17T06:20:29.667Z",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/v1/book-keeping/budget/categories",
  "fieldErrors": [
    {
      "field": "accNo",
      "message": "accNo already exists"
    }
  ]
}
```

## Build and Test

Run tests:

```bash
./mvnw test
```

Build the application:

```bash
./mvnw clean package
```
