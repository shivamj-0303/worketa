-- Convert wage_for_day from DOUBLE PRECISION to NUMERIC(10,2) in attendance table
ALTER TABLE attendance
ALTER COLUMN wage_for_day TYPE NUMERIC(10,2);

-- Drop the old unique constraint on employee_code if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE table_name = 'employees' AND constraint_type = 'UNIQUE' 
               AND constraint_name = 'employees_employee_code_key') THEN
        ALTER TABLE employees DROP CONSTRAINT employees_employee_code_key;
    END IF;
END $$;

-- Add new composite unique constraint on employee_code and organisation_id
ALTER TABLE employees
ADD CONSTRAINT uk_employee_code_org UNIQUE (employee_code, organisation_id);

-- Add organisation_id column to wage_history table
ALTER TABLE wage_history
ADD COLUMN organisation_id uuid NOT NULL;

-- Create indexes for wage_history
CREATE INDEX IF NOT EXISTS idx_wage_history_employee_org ON wage_history(employee_id, organisation_id);
CREATE INDEX IF NOT EXISTS idx_wage_history_effective_from ON wage_history(effective_from);
