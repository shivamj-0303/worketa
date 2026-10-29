DROP INDEX IF EXISTS uq_attendance_emp_date;
CREATE UNIQUE INDEX uq_attendance_emp_date_type
    ON attendance(employee_id, attendance_date, type);
