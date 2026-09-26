ALTER TABLE employees
    ALTER COLUMN absent_deduction_per_day SET DEFAULT 600.00;

UPDATE employees
SET absent_deduction_per_day = 600.00
WHERE absent_deduction_per_day IS NULL
   OR absent_deduction_per_day = 0;