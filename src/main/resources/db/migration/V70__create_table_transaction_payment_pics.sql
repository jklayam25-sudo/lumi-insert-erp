CREATE TABLE transaction_payment_pics (

    id UUID NOT NULL,
    transaction_payment_id UUID NOT NULL,
    picture_url TEXT NOT NULL,
    created_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT fk_transaction_payment_pics FOREIGN KEY (transaction_payment_id)
        REFERENCES transaction_payments(id)

);

CREATE INDEX idx_transaction_payment_pics ON transaction_payment_pics(transaction_payment_id);