-- Allocate generated employee codes from a database sequence so codes are
-- never reused after deletion and remain unique during concurrent creation.
CREATE SEQUENCE IF NOT EXISTS employee_code_seq
    AS BIGINT
    MINVALUE 1
    START WITH 1;

DO $$
DECLARE
    highest_existing_code BIGINT;
BEGIN
    SELECT MAX(CAST(SUBSTRING(employee_code FROM 5) AS BIGINT))
    INTO highest_existing_code
    FROM employees
    WHERE employee_code ~ '^EMP-[0-9]+$';

    IF highest_existing_code IS NOT NULL THEN
        PERFORM setval('employee_code_seq', highest_existing_code, true);
    END IF;
END $$;