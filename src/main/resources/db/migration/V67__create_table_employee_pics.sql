CREATE TABLE employee_pics (

    id UUID NOT NULL,
    employee_id UUID NOT NULL UNIQUE,
    picture_url TEXT NOT NULL,
    created_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(55) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,

    CONSTRAINT fk_employee_profile_pics FOREIGN KEY (employee_id)
        REFERENCES employees(id)

);

CREATE INDEX idx_employee_profile_pics ON employee_pics(employee_id);