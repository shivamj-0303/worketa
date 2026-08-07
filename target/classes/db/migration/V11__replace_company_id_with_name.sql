-- Migration V11: Replace company_id with company_name in trips table
-- Remove the NOT NULL UUID company_id and replace with optional String company_name

DO $$ 
BEGIN
    -- Drop the company_id column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'trips' AND column_name = 'company_id'
    ) THEN
        ALTER TABLE trips DROP COLUMN company_id;
    END IF;
    
    -- Add company_name column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'trips' AND column_name = 'company_name'
    ) THEN
        ALTER TABLE trips ADD COLUMN company_name VARCHAR(255);
    END IF;
END $$;
