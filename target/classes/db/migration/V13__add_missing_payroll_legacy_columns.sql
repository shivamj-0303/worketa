DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'month'
    ) THEN
        ALTER TABLE payroll ADD COLUMN month VARCHAR(7);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'base_salary'
    ) THEN
        ALTER TABLE payroll ADD COLUMN base_salary NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'remarks'
    ) THEN
        ALTER TABLE payroll ADD COLUMN remarks TEXT;
    END IF;
END $$;