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

## Configuration

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
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

## Build and Run

### Option 1: Environment Variables

```bash
export DB_URL=jdbc:postgresql://localhost:5432/book_keeping
export DB_USER=postgres
export DB_PASSWORD=postgres
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Notes
- Module packages are currently placeholders; add services, controllers, and entities within each module as needed.
