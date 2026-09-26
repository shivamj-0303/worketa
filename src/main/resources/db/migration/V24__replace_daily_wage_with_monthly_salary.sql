DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'employees' AND column_name = 'daily_wage')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'employees' AND column_name = 'monthly_salary') THEN
        ALTER TABLE employees RENAME COLUMN daily_wage TO monthly_salary;
        ALTER TABLE employees ALTER COLUMN monthly_salary TYPE NUMERIC(12,2);
        UPDATE employees SET monthly_salary = monthly_salary * 30;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'employees' AND column_name = 'monthly_salary') THEN
        ALTER TABLE employees ADD COLUMN monthly_salary NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'employees' AND column_name = 'absent_deduction_per_day') THEN
        ALTER TABLE employees ADD COLUMN absent_deduction_per_day NUMERIC(10,2) NOT NULL DEFAULT 0;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payroll' AND column_name = 'daily_wage')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payroll' AND column_name = 'monthly_salary') THEN
        ALTER TABLE payroll RENAME COLUMN daily_wage TO monthly_salary;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payroll' AND column_name = 'monthly_salary') THEN
        ALTER TABLE payroll ADD COLUMN monthly_salary NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payroll' AND column_name = 'absent_deduction_per_day') THEN
        ALTER TABLE payroll ADD COLUMN absent_deduction_per_day NUMERIC(10,2) NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payroll' AND column_name = 'absence_deduction') THEN
        ALTER TABLE payroll ADD COLUMN absence_deduction NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'attendance' AND column_name = 'bonus_amount') THEN
        ALTER TABLE attendance ADD COLUMN bonus_amount NUMERIC(10,2) NOT NULL DEFAULT 0;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'wage_history' AND column_name = 'daily_wage')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'wage_history' AND column_name = 'monthly_salary') THEN
        ALTER TABLE wage_history RENAME COLUMN daily_wage TO monthly_salary;
    END IF;
END $$;