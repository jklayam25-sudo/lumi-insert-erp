ALTER TABLE employees
    RENAME COLUMN profile_url to picture_url;

ALTER TABLE employees
    ALTER COLUMN picture_url TYPE TEXT;