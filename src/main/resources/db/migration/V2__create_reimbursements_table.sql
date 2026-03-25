CREATE TYPE reimbursement_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'PAID');

CREATE TABLE reimbursements
(
    id                 BIGSERIAL PRIMARY KEY,
    description        TEXT,
    amount             NUMERIC(19, 2),
    expenditure_date   DATE,
    name               TEXT,
    should_reimburse   BOOLEAN DEFAULT FALSE,
    account_name       TEXT,
    clearing_number    TEXT,
    account_number     TEXT,
    acc_no             VARCHAR(5)               NOT NULL,
    phone_number       TEXT,
    is_correct         BOOLEAN DEFAULT FALSE,
    status             reimbursement_status     NOT NULL DEFAULT 'PENDING',
    processed_at       TIMESTAMP WITH TIME ZONE NULL,
    processed_by       BIGINT                   NULL,
    paid_out_at        TIMESTAMP WITH TIME ZONE NULL,
    paid_out_by        BIGINT                   NULL,
    admin_comment      TEXT                     NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reimbursements_acc_no
        FOREIGN KEY (acc_no)
            REFERENCES budget_categories (acc_no)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);
