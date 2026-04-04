 CREATE TABLE product_pics_aud (
	id UUID NOT NULL,
	rev BIGINT NOT NULL,
	revtype INTEGER NULL,
	picture_url TEXT NULL,
	CONSTRAINT product_pics_aud_pkey PRIMARY KEY (rev, id)
);
 
ALTER TABLE public.product_pics_aud ADD CONSTRAINT fk_product_pics_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev); 