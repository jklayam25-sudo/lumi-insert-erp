CREATE TABLE product_pics (

    id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    picture_url TEXT NOT NULL,
    created_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT fk_product_pic FOREIGN KEY (product_id)
        REFERENCES products(id)

);

CREATE INDEX idx_product_pics ON product_pics(product_id);