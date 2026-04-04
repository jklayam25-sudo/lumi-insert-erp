 CREATE TABLE employee_pics_aud (
	id UUID NOT NULL,
	rev BIGINT NOT NULL,
	revtype INTEGER NULL,
	picture_url TEXT NULL,
	CONSTRAINT employee_pics_aud_pkey PRIMARY KEY (rev, id)
);
 
ALTER TABLE public.employee_pics_aud ADD CONSTRAINT fk_employee_pics_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev); 