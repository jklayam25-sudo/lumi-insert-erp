CREATE TABLE supply_payment_pics (

    id UUID NOT NULL,
    supply_payment_id UUID NOT NULL,
    picture_url TEXT NOT NULL,
    created_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT fk_supply_payment_pics FOREIGN KEY (supply_payment_id)
        REFERENCES supply_payments(id)

);

CREATE INDEX idx_supply_payment_pics ON supply_payment_pics(supply_payment_id);