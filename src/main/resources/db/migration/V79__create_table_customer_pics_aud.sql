 CREATE TABLE customer_pics_aud (
	id UUID NOT NULL,
	rev BIGINT NOT NULL,
	revtype INTEGER NULL,
	picture_url TEXT NULL,
	CONSTRAINT customer_pics_aud_pkey PRIMARY KEY (rev, id)
);
 
ALTER TABLE public.customer_pics_aud ADD CONSTRAINT fk_customer_pics_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev); 