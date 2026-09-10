-- Remove records owned by an employee when the employee is deleted.
ALTER TABLE wage_history
    DROP CONSTRAINT IF EXISTS fk_wage_history_emp,
    ADD CONSTRAINT fk_wage_history_emp
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE trips
    DROP CONSTRAINT IF EXISTS fk_trips_driver,
    ADD CONSTRAINT fk_trips_driver
        FOREIGN KEY (driver_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE trips
    DROP CONSTRAINT IF EXISTS fk_trips_assistant,
    ADD CONSTRAINT fk_trips_assistant
        FOREIGN KEY (assistant_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE attendance
    DROP CONSTRAINT IF EXISTS fk_attendance_emp,
    ADD CONSTRAINT fk_attendance_emp
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE advances
    DROP CONSTRAINT IF EXISTS fk_advances_emp,
    ADD CONSTRAINT fk_advances_emp
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE payroll
    DROP CONSTRAINT IF EXISTS fk_payroll_emp,
    ADD CONSTRAINT fk_payroll_emp
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;