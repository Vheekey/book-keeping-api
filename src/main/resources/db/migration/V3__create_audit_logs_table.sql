CREATE TABLE audit_logs
(
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(100)             NOT NULL,
    entity_id    VARCHAR(100)             NOT NULL,
    action       VARCHAR(100)             NOT NULL,
    actor        VARCHAR(255),
    details_json JSONB,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
