ALTER TABLE employee_pics_aud
    ADD COLUMN public_id TEXT NULL;

ALTER TABLE product_pics_aud
    ADD COLUMN public_id TEXT NULL;

ALTER TABLE customer_pics_aud
    ADD COLUMN public_id TEXT NULL;

ALTER TABLE transaction_payment_pics_aud
    ADD COLUMN public_id TEXT NULL;

ALTER TABLE supply_payment_pics_aud
    ADD COLUMN public_id TEXT NULL;