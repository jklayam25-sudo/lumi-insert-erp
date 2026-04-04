CREATE TABLE customer_pics (

    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    picture_url TEXT NOT NULL,
    created_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT fk_customer_pic FOREIGN KEY (customer_id)
        REFERENCES customers(id)

);

CREATE INDEX idx_customer_pics ON customer_pics(customer_id);