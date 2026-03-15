CREATE TABLE budget_categories
(
    acc_no      VARCHAR(5) PRIMARY KEY,
    description TEXT NOT NULL,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);