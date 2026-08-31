DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'advance_deduction'
    ) THEN
        ALTER TABLE payroll ADD COLUMN advance_deduction NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'gross_amount'
    ) THEN
        ALTER TABLE payroll ADD COLUMN gross_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;
END $$;
