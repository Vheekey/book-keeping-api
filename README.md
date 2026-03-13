# Book Keeping API

## Overview
Book Keeping API is a Spring Boot service for managing finance operations across reimbursements, budgeting, approvals, accounting, and audit logging.

## Modules

- reimbursement = request submission
- accounting = ledger posting
- budget = ministry category allocation
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

Default database settings live in `src/main/resources/application.properties`:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/book_keeping`
- `spring.datasource.username=postgres`
- `spring.datasource.password=postgres`

## Build and Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Notes
- Module packages are currently placeholders; add services, controllers, and entities within each module as needed.
