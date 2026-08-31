DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'bonus_amount'
    ) THEN
        ALTER TABLE payroll ADD COLUMN bonus_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;
END $$;
