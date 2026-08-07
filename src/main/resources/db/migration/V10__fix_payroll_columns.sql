-- Fix payroll columns - add those that don't exist yet
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'payroll' AND column_name = 'daily_wage') THEN
        ALTER TABLE payroll ADD COLUMN daily_wage NUMERIC(10,2) DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'payroll' AND column_name = 'present_days') THEN
        ALTER TABLE payroll ADD COLUMN present_days INTEGER DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'payroll' AND column_name = 'doubled_days') THEN
        ALTER TABLE payroll ADD COLUMN doubled_days INTEGER DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'payroll' AND column_name = 'absent_days') THEN
        ALTER TABLE payroll ADD COLUMN absent_days INTEGER DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'payroll' AND column_name = 'deductions') THEN
        ALTER TABLE payroll ADD COLUMN deductions NUMERIC(12,2) DEFAULT 0;
    END IF;
END $$;
