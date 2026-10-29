WITH ranked_attendance AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY employee_id, attendance_date
               ORDER BY
                   CASE WHEN status = 'APPROVED' THEN 0 ELSE 1 END,
                   updated_at DESC NULLS LAST,
                   created_at DESC NULLS LAST,
                   id DESC
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
DROP INDEX IF EXISTS uq_attendance_emp_date;
CREATE UNIQUE INDEX uq_attendance_emp_date
    ON attendance(employee_id, attendance_date);