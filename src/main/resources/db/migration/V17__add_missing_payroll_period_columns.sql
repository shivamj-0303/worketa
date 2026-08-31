DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'period_start'
    ) THEN
        ALTER TABLE payroll ADD COLUMN period_start DATE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'period_end'
    ) THEN
        ALTER TABLE payroll ADD COLUMN period_end DATE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'daily_wage'
    ) THEN
        ALTER TABLE payroll ADD COLUMN daily_wage NUMERIC(10,2) DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'gross_amount'
    ) THEN
        ALTER TABLE payroll ADD COLUMN gross_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'bonus_amount'
    ) THEN
        ALTER TABLE payroll ADD COLUMN bonus_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'advance_deduction'
    ) THEN
        ALTER TABLE payroll ADD COLUMN advance_deduction NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payroll' AND column_name = 'net_amount'
    ) THEN
        ALTER TABLE payroll ADD COLUMN net_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
    END IF;
END $$;
