ALTER TABLE customers ALTER COLUMN total_unpaid TYPE NUMERIC(19, 4) USING total_unpaid::numeric;
ALTER TABLE customers ALTER COLUMN total_paid TYPE NUMERIC(19, 4) USING total_paid::numeric;
ALTER TABLE customers ALTER COLUMN total_unrefunded TYPE NUMERIC(19, 4) USING total_unrefunded::numeric;
ALTER TABLE customers ALTER COLUMN total_refunded TYPE NUMERIC(19, 4) USING total_refunded::numeric;

ALTER TABLE products ALTER COLUMN base_price TYPE NUMERIC(19, 4) USING base_price::numeric;
ALTER TABLE products ALTER COLUMN sell_price TYPE NUMERIC(19, 4) USING sell_price::numeric;
ALTER TABLE products ALTER COLUMN stock_quantity TYPE NUMERIC(19, 4) USING stock_quantity::numeric;
ALTER TABLE products ALTER COLUMN stock_minimum TYPE NUMERIC(19, 4) USING stock_minimum::numeric;

ALTER TABLE stock_cards ALTER COLUMN quantity TYPE NUMERIC(19, 4) USING quantity::numeric;
ALTER TABLE stock_cards ALTER COLUMN old_stock TYPE NUMERIC(19, 4) USING old_stock::numeric;
ALTER TABLE stock_cards ALTER COLUMN new_stock TYPE NUMERIC(19, 4) USING new_stock::numeric;
ALTER TABLE stock_cards ALTER COLUMN old_price TYPE NUMERIC(19, 4) USING old_price::numeric;
ALTER TABLE stock_cards ALTER COLUMN new_price TYPE NUMERIC(19, 4) USING new_price::numeric;

ALTER TABLE suppliers ALTER COLUMN total_unpaid TYPE NUMERIC(19, 4) USING total_unpaid::numeric;
ALTER TABLE suppliers ALTER COLUMN total_paid TYPE NUMERIC(19, 4) USING total_paid::numeric;
ALTER TABLE suppliers ALTER COLUMN total_unrefunded TYPE NUMERIC(19, 4) USING total_unrefunded::numeric;
ALTER TABLE suppliers ALTER COLUMN total_refunded TYPE NUMERIC(19, 4) USING total_refunded::numeric;

ALTER TABLE supplies ALTER COLUMN total_fee TYPE NUMERIC(19, 4) USING total_fee::numeric;
ALTER TABLE supplies ALTER COLUMN total_discount TYPE NUMERIC(19, 4) USING total_discount::numeric;
ALTER TABLE supplies ALTER COLUMN sub_total TYPE NUMERIC(19, 4) USING sub_total::numeric;
ALTER TABLE supplies ALTER COLUMN grand_total TYPE NUMERIC(19, 4) USING grand_total::numeric;
ALTER TABLE supplies ALTER COLUMN total_unpaid TYPE NUMERIC(19, 4) USING total_unpaid::numeric;
ALTER TABLE supplies ALTER COLUMN total_paid TYPE NUMERIC(19, 4) USING total_paid::numeric;
ALTER TABLE supplies ALTER COLUMN total_unrefunded TYPE NUMERIC(19, 4) USING total_unrefunded::numeric;
ALTER TABLE supplies ALTER COLUMN total_refunded TYPE NUMERIC(19, 4) USING total_refunded::numeric;

ALTER TABLE supply_items ALTER COLUMN quantity TYPE NUMERIC(19, 4) USING quantity::numeric;
ALTER TABLE supply_items ALTER COLUMN price TYPE NUMERIC(19, 4) USING price::numeric;

ALTER TABLE supply_payments ALTER COLUMN total_payment TYPE NUMERIC(19, 4) USING total_payment::numeric;

ALTER TABLE transactions ALTER COLUMN total_fee TYPE NUMERIC(19, 4) USING total_fee::numeric;
ALTER TABLE transactions ALTER COLUMN total_discount TYPE NUMERIC(19, 4) USING total_discount::numeric;
ALTER TABLE transactions ALTER COLUMN sub_total TYPE NUMERIC(19, 4) USING sub_total::numeric;
ALTER TABLE transactions ALTER COLUMN grand_total TYPE NUMERIC(19, 4) USING grand_total::numeric;
ALTER TABLE transactions ALTER COLUMN total_unpaid TYPE NUMERIC(19, 4) USING total_unpaid::numeric;
ALTER TABLE transactions ALTER COLUMN total_paid TYPE NUMERIC(19, 4) USING total_paid::numeric;
ALTER TABLE transactions ALTER COLUMN total_unrefunded TYPE NUMERIC(19, 4) USING total_unrefunded::numeric;
ALTER TABLE transactions ALTER COLUMN total_refunded TYPE NUMERIC(19, 4) USING total_refunded::numeric;

ALTER TABLE transaction_items ALTER COLUMN quantity TYPE NUMERIC(19, 4) USING quantity::numeric;
ALTER TABLE transaction_items ALTER COLUMN price TYPE NUMERIC(19, 4) USING price::numeric;

ALTER TABLE transaction_payments ALTER COLUMN total_payment TYPE NUMERIC(19, 4) USING total_payment::numeric;