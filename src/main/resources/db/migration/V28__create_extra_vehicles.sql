CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE extra_vehicles (
    id UUID PRIMARY KEY DEFAULT extensions.uuid_generate_v4(),
    organisation_id UUID NOT NULL,
    driver_name VARCHAR(255) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    vehicle_date DATE NOT NULL,
    vehicle_number VARCHAR(255) NOT NULL,
    route_name VARCHAR(255) NOT NULL,
    vehicle_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_extra_vehicles_org FOREIGN KEY (organisation_id) REFERENCES organisations(id)
);

CREATE INDEX idx_extra_vehicles_org ON extra_vehicles(organisation_id);
CREATE INDEX idx_extra_vehicles_date ON extra_vehicles(vehicle_date);