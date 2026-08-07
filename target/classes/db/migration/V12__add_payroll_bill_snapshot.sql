DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'employee_name'
    ) THEN
        ALTER TABLE payroll ADD COLUMN employee_name VARCHAR(255);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'settled_at'
    ) THEN
        ALTER TABLE payroll ADD COLUMN settled_at TIMESTAMP WITH TIME ZONE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'advances_snapshot'
    ) THEN
        ALTER TABLE payroll ADD COLUMN advances_snapshot TEXT;
    END IF;
END $$;