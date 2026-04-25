CREATE TABLE reimbursement_receipts
(
    id                    BIGSERIAL PRIMARY KEY,
    reimbursement_id      BIGINT                   NOT NULL,
    status                VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
    original_filename     TEXT                     NULL,
    source_content_type   TEXT                     NULL,
    original_size_bytes   BIGINT                   NULL,
    staged_path           TEXT                     NULL,
    stored_path           TEXT                     NULL,
    stored_content_type   TEXT                     NULL,
    stored_size_bytes     BIGINT                   NULL,
    sha256                CHAR(64)                 NULL,
    error_message         TEXT                     NULL,
    processed_at          TIMESTAMP WITH TIME ZONE NULL,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reimbursement_receipts_reimbursement_id
        FOREIGN KEY (reimbursement_id)
            REFERENCES reimbursements (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE INDEX idx_reimbursement_receipts_reimbursement_id
    ON reimbursement_receipts (reimbursement_id);

CREATE INDEX idx_reimbursement_receipts_status
    ON reimbursement_receipts (status);
