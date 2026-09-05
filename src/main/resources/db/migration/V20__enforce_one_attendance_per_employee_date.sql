WITH ranked_attendance AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY employee_id, attendance_date
               ORDER BY updated_at DESC, created_at DESC, id DESC
           ) AS row_number
    FROM attendance
)
DELETE FROM attendance
WHERE id IN (
    SELECT id
    FROM ranked_attendance
    WHERE row_number > 1
);

DROP INDEX IF EXISTS uq_attendance_emp_date_type;
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_emp_date
    ON attendance(employee_id, attendance_date);
