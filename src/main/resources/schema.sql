CREATE TABLE IF NOT EXISTS transactions (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id        BIGINT NOT NULL,
    customer_name      VARCHAR(100) NOT NULL,
    transaction_amount DECIMAL(12, 2) NOT NULL CHECK (transaction_amount >= 0),
    transaction_date   DATE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_customer_date ON transactions (customer_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions (transaction_date);