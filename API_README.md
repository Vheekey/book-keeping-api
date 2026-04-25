# Book Keeping API Reference

API reference for the Book Keeping service.

## Base Path

All endpoint paths below are relative to:

```text
/api/v1/book-keeping
```

For local development, the default URL is:

```text
http://localhost:8080/api/v1/book-keeping
```

## Authentication and Authorization

JWT authentication is stateless. Send protected requests with:

```text
Authorization: Bearer <token>
```

Public endpoints:

- `POST /users`
- `POST /users/auth/login`
- `POST /reimbursements/create`
- `POST /reimbursements/{reimbursementId}/receipts`
- `GET /budget/categories/active`

Role-protected endpoints use Spring method security:

- `SADMIN`: user and role administration
- `ADMIN`: admin-created users through `POST /users/create`
- `FINANCE`: reimbursement approval, payout, and receipt review/download actions

Known role codes are `USER`, `SADMIN`, `ADMIN`, and `FINANCE`.

## Auth and Users

### `POST /users`

Creates a public user account and returns an auth response.

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret",
  "username": "johndoe"
}
```

### `POST /users/auth/login`

Logs in with either `username` or `email`.

```json
{
  "username": "johndoe",
  "password": "secret"
}
```

```json
{
  "email": "john@example.com",
  "password": "secret"
}
```

### `POST /users/auth/logout`

Logs out the current request context. Authentication is still JWT-based and stateless.

### `POST /users/create`

`SADMIN` or `ADMIN` only. Creates a user from an administrative context.

### `GET /users`

`SADMIN` only. Returns paginated users.

Query parameters:

- `pageNumber` default: `0`
- `pageSize` default: `10`
- `search` optional

### `GET /users/{userId}`

`SADMIN` only. Returns one user.

### `PUT /users/{userId}`

`SADMIN` only. Updates user name, email, and username.

### `PUT /users/{userId}/change-status`

`SADMIN` only. Toggles a user's active status.

### `POST /users/{userId}/role`

`SADMIN` only. Changes a user's role.

```json
{
  "role": "FINANCE"
}
```

### `DELETE /users/{userId}`

`SADMIN` only. Deletes a user.

## Roles

### `GET /roles`

`SADMIN` only. Returns all roles.

### `GET /roles/{roleId}`

`SADMIN` only. Returns one role.

### `POST /roles`

`SADMIN` only. Creates a role.

```json
{
  "name": "FINANCE"
}
```

### `PUT /roles/{roleId}`

`SADMIN` only. Updates a role.

### `DELETE /roles/{roleId}`

`SADMIN` only. Deletes a role.

### `POST /roles/{roleId}/users/{userId}`

`SADMIN` only. Assigns a role to a user.

## Budget Categories

### `GET /budget/categories`

Returns all budget categories.

### `GET /budget/categories/active`

Public. Returns active budget categories.

### `POST /budget/categories`

Creates a budget category.

```json
{
  "accNo": "1001",
  "description": "Transport"
}
```

### `PUT /budget/categories/{accNo}/change-status`

Toggles the active status for the category identified by `accNo`.

## Reimbursements

### `POST /reimbursements/create`

Public. Creates a reimbursement request.

```json
{
  "expenditureDate": "2026-04-01",
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

### `GET /reimbursements`

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

### `GET /reimbursements/{reimbursementId}`

Returns a single reimbursement by ID.

### `POST /reimbursements/{reimbursementId}/approve`

`FINANCE` only. Approves or rejects a reimbursement.

```json
{
  "comment": "Approved for payout",
  "isApproved": "true"
}
```

### `POST /reimbursements/{reimbursementId}/payout`

`FINANCE` only. Marks an approved reimbursement as paid.

## Receipts

### `POST /reimbursements/{reimbursementId}/receipts`

Public. Uploads a receipt for a reimbursement and queues it for background processing.

Send the request as `multipart/form-data` with a `receipt` file field. JPEG and PNG receipts are processed into
compressed JPEG files; PDFs are stored as files. The database stores receipt metadata, processing status, and storage
paths outside the reimbursement record.

### `GET /reimbursements/{reimbursementId}/receipts`

`FINANCE` only. Returns receipt metadata for a reimbursement.

### `GET /reimbursements/{reimbursementId}/receipts/{receiptId}`

`FINANCE` only. Returns one receipt metadata record.

### `GET /reimbursements/{reimbursementId}/receipts/{receiptId}/file`

`FINANCE` only. Streams the processed receipt file after processing is complete.

### `POST /reimbursements/{reimbursementId}/receipts/{receiptId}/retry`

`FINANCE` only. Queues receipt processing again unless the receipt is already processing.

### `DELETE /reimbursements/{reimbursementId}/receipts/{receiptId}`

`FINANCE` only. Deletes the receipt metadata record unless the receipt is currently processing. Stored files are not
deleted because processed files may be shared by hash.

Receipt processing statuses:

- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `FAILED`

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
