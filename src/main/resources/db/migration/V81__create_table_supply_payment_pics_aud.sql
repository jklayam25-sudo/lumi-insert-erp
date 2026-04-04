 CREATE TABLE supply_payment_pics_aud (
	id UUID NOT NULL,
	rev BIGINT NOT NULL,
	revtype INTEGER NULL,
	picture_url TEXT NULL,
	CONSTRAINT supply_payment_pics_aud_pkey PRIMARY KEY (rev, id)
);
 
ALTER TABLE public.supply_payment_pics_aud ADD CONSTRAINT fk_supply_payment_pics_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev); 