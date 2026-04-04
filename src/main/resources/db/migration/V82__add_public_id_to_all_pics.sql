ALTER TABLE employee_pics
    ADD COLUMN public_id TEXT NOT NULL;

ALTER TABLE product_pics
    ADD COLUMN public_id TEXT NOT NULL;

ALTER TABLE customer_pics
    ADD COLUMN public_id TEXT NOT NULL;

ALTER TABLE transaction_payment_pics
    ADD COLUMN public_id TEXT NOT NULL;

ALTER TABLE supply_payment_pics
    ADD COLUMN public_id TEXT NOT NULL;