CREATE TABLE reimbursements
(
    id               BIGSERIAL PRIMARY KEY,
    description      TEXT,
    amount           NUMERIC(19, 2),
    expenditure_date DATE,
    name             TEXT,
    should_reimburse BOOLEAN DEFAULT FALSE,
    account_name     TEXT,
    clearing_number  TEXT,
    account_number   TEXT,
    acc_no           VARCHAR(5) NOT NULL,
    phone_number     TEXT,
    is_correct       BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reimbursements_acc_no
        FOREIGN KEY (acc_no)
            REFERENCES budget_categories (acc_no)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);
