-- Employees
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    employee_code VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    type VARCHAR(50) NOT NULL,
    joining_date DATE NOT NULL,
    leaving_date DATE,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_employees_org FOREIGN KEY (organisation_id) REFERENCES organisations(id)
);
CREATE INDEX idx_employees_org ON employees(organisation_id);

-- Wage History
CREATE TABLE wage_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL,
    daily_wage NUMERIC(10,2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_wage_history_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);
CREATE INDEX idx_wage_history_emp ON wage_history(employee_id);

-- Vehicles
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    vehicle_number VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN DEFAULT true,
    active_from DATE,
    inactive_from DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_vehicles_org FOREIGN KEY (organisation_id) REFERENCES organisations(id)
);
CREATE INDEX idx_vehicles_org ON vehicles(organisation_id);

-- Companies
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    address TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_companies_org FOREIGN KEY (organisation_id) REFERENCES organisations(id)
);
CREATE INDEX idx_companies_org ON companies(organisation_id);

-- Trips
CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    driver_id UUID NOT NULL,
    assistant_id UUID,
    company_id UUID NOT NULL,
    route TEXT,
    trip_date DATE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_trips_org FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_trips_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_trips_driver FOREIGN KEY (driver_id) REFERENCES employees(id),
    CONSTRAINT fk_trips_assistant FOREIGN KEY (assistant_id) REFERENCES employees(id),
    CONSTRAINT fk_trips_company FOREIGN KEY (company_id) REFERENCES companies(id)
);
CREATE INDEX idx_trips_org ON trips(organisation_id);
CREATE INDEX idx_trips_vehicle ON trips(vehicle_id);
CREATE INDEX idx_trips_driver ON trips(driver_id);
CREATE INDEX idx_trips_date_vehicle ON trips(trip_date, vehicle_id);

-- Attendance
CREATE TABLE attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    checkin_time TIMESTAMP WITH TIME ZONE,
    checkout_time TIMESTAMP WITH TIME ZONE,
    present BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_attendance_org FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_attendance_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);
CREATE INDEX idx_attendance_org ON attendance(organisation_id);
CREATE INDEX idx_attendance_emp ON attendance(employee_id);
CREATE UNIQUE INDEX uq_attendance_emp_date ON attendance(employee_id, attendance_date);

-- Advances
CREATE TABLE advances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    advance_date DATE NOT NULL,
    note TEXT,
    settled BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_advances_org FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_advances_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);
CREATE INDEX idx_advances_org ON advances(organisation_id);
CREATE INDEX idx_advances_emp ON advances(employee_id);

-- Payroll
CREATE TABLE payroll (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    month VARCHAR(7) NOT NULL,
    base_salary NUMERIC(12,2) NOT NULL,
    advances NUMERIC(12,2) DEFAULT 0,
    net_amount NUMERIC(12,2) NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_payroll_org FOREIGN KEY (organisation_id) REFERENCES organisations(id),
    CONSTRAINT fk_payroll_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);
CREATE INDEX idx_payroll_org ON payroll(organisation_id);
CREATE INDEX idx_payroll_emp ON payroll(employee_id);
CREATE UNIQUE INDEX uq_payroll_emp_month ON payroll(employee_id, month);
