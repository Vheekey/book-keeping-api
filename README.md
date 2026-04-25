# Book Keeping API

Spring Boot API for finance book keeping workflows: reimbursements, receipt uploads, budget categories, users, roles,
and audit logging.

## Tech Stack

- Java 17
- Spring Boot 3.3
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Security with JWT bearer tokens
- Flyway migrations

## Project Layout

```text
src/main/java/com/calvary/finance/
├── FinanceApplication.java
├── audit/                 # audit log persistence and services
├── budget/category/       # budget category API, validation, and persistence
├── receipt/               # receipt upload, storage, processing, and metadata APIs
├── reimbursement/         # reimbursement requests, approvals, payout, and queries
├── shared/                # JWT, security, CORS, errors, and exception handling
└── user/                  # users, roles, auth, and role assignment
```

Database scripts live under:

```text
src/main/resources/db/
├── migration/             # Flyway versioned migrations
├── rollback/              # manual rollback scripts
└── seeders/               # optional seed data
```

## Prerequisites

- Java 17+
- PostgreSQL
- Maven wrapper from this repository (`./mvnw`)

## Configuration

The API base path is:

```text
/api/v1/book-keeping
```

Runtime configuration is read from `src/main/resources/application.properties`. For local overrides, copy the example
file and run with the `local` profile:

```bash
cp src/main/resources/application-example.properties src/main/resources/application-local.properties
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Supported environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/book_keeping` | PostgreSQL JDBC URL |
| `DB_USER` | varies by properties file | PostgreSQL username |
| `DB_PASSWORD` | varies by properties file | PostgreSQL password |
| `JWT_SECRET` | required by `application-example.properties` | Base64-compatible JWT signing secret |
| `JWT_EXPIRATION_MS` | `86400000` | JWT lifetime in milliseconds |
| `RECEIPT_STORAGE_PATH` | `uploads/receipts` | Receipt file storage root |
| `RECEIPT_MAX_UPLOAD_BYTES` | `10485760` | Maximum receipt upload size, 10 MiB by default |
| `RECEIPT_MAX_IMAGE_DIMENSION` | `1600` | Maximum image width or height after processing |
| `RECEIPT_JPEG_QUALITY` | `0.75` | JPEG compression quality for processed image receipts |

Important defaults:

- `spring.jpa.hibernate.ddl-auto=none`
- `spring.flyway.enabled=false`
- CORS allows `http://localhost:5173` and `http://127.0.0.1:5173`

## Run Locally

Create the database first, then configure credentials:

```bash
createdb book_keeping

export DB_URL=jdbc:postgresql://localhost:5432/book_keeping
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=V3RTZWNyZXRLZXlGb3JKV1RUaGF0SXNMb25nRW5vdWdo

./mvnw spring-boot:run
```

The API will be available at:

```text
http://localhost:8080/api/v1/book-keeping
```

## Database Migrations

Flyway auto-run is currently disabled, so migrations are intended to be run manually:

```bash
./mvnw \
  -Dflyway.url=jdbc:postgresql://localhost:5432/book_keeping \
  -Dflyway.user=postgres \
  -Dflyway.password=postgres \
  flyway:migrate
```

## API Reference

Authentication, authorization, endpoints, request examples, and error response details are documented in
[`API_README.md`](API_README.md).

## Build and Test

Run tests:

```bash
./mvnw test
```

Build the application:

```bash
./mvnw clean package
```

## Docker Deployment

This repository includes:

- `Dockerfile` for a production-style multi-stage image build
- `compose.yaml` for the API plus PostgreSQL
- `.env.docker.example` as a deployment environment template

### Option 1: Run the Full Stack with Docker Compose

Create a deployment env file:

```bash
cp .env.docker.example .env
```

Update at least `JWT_SECRET` before deploying, then start the stack:

```bash
docker compose up --build -d
```

The API will be available at:

```text
http://localhost:8080/api/v1/book-keeping
```

What the compose stack provides:

- PostgreSQL 16 with a persistent `postgres-data` volume
- The Spring Boot API with a persistent `receipt-data` volume
- Automatic startup ordering using a PostgreSQL health check
- Flyway enabled in the API container by default via `SPRING_FLYWAY_ENABLED=true`

To stop the stack:

```bash
docker compose down
```

To stop it and remove volumes:

```bash
docker compose down -v
```

### Option 2: Build and Run Only the API Image

Build the image:

```bash
docker build -t book-keeping-api:latest .
```

Run it against an existing PostgreSQL instance:

```bash
docker run --rm \
  --name book-keeping-api \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/book_keeping \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=replace-with-a-long-random-base64-compatible-secret \
  -e SPRING_FLYWAY_ENABLED=true \
  -v book-keeping-receipts:/app/uploads/receipts \
  book-keeping-api:latest
```

Receipt files are stored at `/app/uploads/receipts` inside the container, so keep that path mounted for persistence.
