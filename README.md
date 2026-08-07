# WORKETA - Transport Management System Backend

## Overview

A production-grade, multi-tenant Transport Management System backend built with Spring Boot 3.x, Java 21, PostgreSQL, and Flyway. Designed for enterprise-scale transport companies with comprehensive features for vehicle management, employee tracking, trip planning, attendance, payroll, and financial advances.

## Quick Start

### Prerequisites
- Java 21+
- PostgreSQL 13+
- Maven 3.8+
- Docker & Docker Compose (optional)

### Development Environment

```bash
# Start PostgreSQL using Docker Compose
docker-compose up -d

# Build the project
mvn clean package -DskipTests

# Run the application
java -jar target/worketa-0.0.1-SNAPSHOT.jar
```

The backend will be available at `http://localhost:8080`.

## Architecture

- **Multi-Tenant**: All data isolated per `organisation_id`
- **Modular**: Feature-based package structure (auth, employees, vehicles, trips, etc.)
- **Secure**: JWT authentication with role-based access control
- **Transactional**: Ensures data consistency across operations
- **Validated**: Service-layer and database-level constraints
- **Non-Deletion**: Soft deletes for employees and vehicles

## API Endpoints

### Authentication
- `POST /api/organisations/register` - Register new organisation
- `POST /api/auth/login` - User login (returns access + refresh tokens)
- `POST /api/auth/refresh` - Refresh access token

### Users
- `POST /api/users` - Create user (admin only)
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user details

### Employees
- `POST /api/employees` - Create employee
- `GET /api/employees` - List employees
- `GET /api/employees/{id}` - Get employee
- `POST /api/employees/{id}/mark-inactive` - Mark employee inactive

### Vehicles
- `POST /api/vehicles` - Create vehicle
- `GET /api/vehicles` - List vehicles
- `GET /api/vehicles/{id}` - Get vehicle
- `POST /api/vehicles/{id}/mark-inactive` - Mark vehicle inactive

### Companies
- `POST /api/companies` - Create company
- `GET /api/companies` - List companies
- `GET /api/companies/{id}` - Get company
- `PUT /api/companies/{id}` - Update company
- `DELETE /api/companies/{id}` - Soft delete company

### Trips
- `POST /api/trips` - Create trip (validates overlap and inactive resources)
- `GET /api/trips` - List trips
- `GET /api/trips/{id}` - Get trip

### Attendance
- `POST /api/attendance` - Mark attendance
- `GET /api/attendance` - List attendance records

### Advances
- `POST /api/advances` - Create advance
- `GET /api/advances` - List advances by organisation

### Payroll
- `POST /api/payroll` - Create payroll settlement
- `GET /api/payroll` - List payroll records

## Database Schema (Flyway Migrations)

- **V1**: Initial schema (organisations, users, refresh_tokens)
- **V2**: Unique constraints and indexes
- **V3**: Roles and user_roles (proper role management)
- **V4**: Refresh token storage
- **V5**: All business modules (employees, vehicles, companies, trips, attendance, advances, payroll)

## Security

- **Authentication**: JWT tokens (15 min access, 7 day refresh)
- **Password Hashing**: BCrypt (strength 10)
- **Authorization**: Role-based (SUPER_ADMIN, ADMIN, OPERATOR, ACCOUNTANT)
- **Multi-Tenancy**: All queries scoped to organisation_id
- **Protected Endpoints**: All except registration, login, refresh

## Testing

```bash
# Unit tests
mvn test -Dspring.profiles.active=test

# Full build with tests
mvn clean verify
```

## Configuration

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/worketa
export SPRING_DATASOURCE_USERNAME=worketa
export SPRING_DATASOURCE_PASSWORD=worketa
export SECURITY_JWT_SECRET=<very-long-secret-key-for-production>
```

### Or update `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/worketa
spring.datasource.username=worketa
spring.datasource.password=worketa
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
security.jwt.secret=<env-var-recommended>
```

## Project Structure

```
src/main/java/com/worketa/
├── common/
│   ├── config/              # ApplicationConfig, JwtProperties
│   ├── security/            # JwtTokenProvider, SecurityConfig, JwtAuthenticationFilter, OrganisationContext
│   ├── exception/           # ApiException, GlobalExceptionHandler
│   ├── response/            # ApiResponse (standard response wrapper)
│   ├── audit/               # Auditable base entity
│
└── modules/
    ├── organisation/        # Organisation CRUD, default admin creation
    ├── auth/                # Login, refresh, JWT handling
    ├── users/               # User management, role assignment
    ├── employees/           # Employee CRUD with wage history
    ├── vehicles/            # Vehicle fleet management
    ├── companies/           # Client/company management
    ├── trips/               # Trip planning with validation
    ├── attendance/          # Attendance tracking
    ├── advances/            # Employee advances
    └── payroll/             # Payroll settlement
```

## Key Features Implemented

✅ Multi-tenant organisation isolation  
✅ JWT-based authentication with refresh tokens  
✅ Role-based authorization (ADMIN, OPERATOR, ACCOUNTANT, SUPER_ADMIN)  
✅ Employee management with wage history  
✅ Vehicle fleet management with non-deletion pattern  
✅ Trip overlap validation (vehicle + driver + time)  
✅ Attendance tracking  
✅ Employee advance management  
✅ Payroll settlement with deductions  
✅ Comprehensive validation (service + database constraints)  
✅ Transactional consistency  
✅ Flyway database migrations  
✅ Global exception handling  
✅ Standard API response format  

## Build & Deployment

### Local Development
```bash
docker-compose up -d
mvn spring-boot:run
```

### Docker Deployment
```bash
docker build -t worketa:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/worketa \
  -e SECURITY_JWT_SECRET=<secret> \
  worketa:latest
```

---

**Version**: 1.0.0 (May 22, 2026)  
**Status**: Production-Ready  
**Last Updated**: May 22, 2026


<!-- 'use client'

import { useMemo, useState, useEffect } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useApiClient } from '@/hooks/useApiClient'
import Button from '@/components/ui/Button'

interface EmployeeData {
  id: string
  fullName: string
  type: string
  dailyWage: number
}

interface AttendanceRecord {
  id: string
  employeeId: string
  attendanceDate: string
  type: 'PRESENT' | 'ABSENT' | 'WORKED_DOUBLE'
}

export default function AttendancePage() {
  const apiClient = useApiClient()

  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  )
  const [searchQuery, setSearchQuery] = useState('')

  // pending UI edits (NOT DB state)
  const [pendingAttendance, setPendingAttendance] = useState<
    Record<string, any>
  >({})

  const [attendanceMap, setAttendanceMap] = useState<{
    [key: string]: AttendanceRecord
  }>({})

  // ---------------- EMPLOYEES ----------------
  const { data: empResponse, isLoading: empLoading } = useQuery({
    queryKey: ['employees'],
    queryFn: async () => apiClient.get('/v1/employees'),
  })

  const employees = useMemo(() => {
    if (
      empResponse &&
      'success' in empResponse &&
      empResponse.success &&
      'data' in empResponse
    ) {
      const data = (empResponse as any).data
      return Array.isArray(data) ? (data as EmployeeData[]) : []
    }
    return []
  }, [empResponse])

  // ---------------- ATTENDANCE FETCH ----------------
  const { data: attResponse, refetch: refetchAttendance } = useQuery({
    queryKey: ['attendance', selectedDate],
    queryFn: async () =>
      apiClient.get(`/v1/attendance?date=${selectedDate}`),
  })

  const attendanceRecords = useMemo(() => {
    if (
      attResponse &&
      'success' in attResponse &&
      attResponse.success &&
      'data' in attResponse
    ) {
      const data = (attResponse as any).data
      const records = Array.isArray(data) ? data : []

      const map: Record<string, AttendanceRecord> = {}
      records.forEach((r) => {
        map[r.employeeId] = r
      })

      return map
    }
    return {}
  }, [attResponse])

  useEffect(() => {
    setAttendanceMap(attendanceRecords)
  }, [attendanceRecords])

  // ---------------- MUTATION ----------------
  const markAttendanceMutation = useMutation({
    mutationFn: async ({
      employeeId,
      type,
    }: {
      employeeId: string
      type: 'PRESENT' | 'ABSENT' | 'WORKED_DOUBLE'
      employeeWage: number
    }) => {

      const payload = {
        employeeId,
        attendanceDate: selectedDate,
        type,
      }

      const existing = attendanceMap[employeeId]

      // FIXED: correct update vs create logic
      if (existing?.id) {
        return apiClient.put(`/v1/attendance/${existing.id}`, payload)
      }

      return apiClient.post('/v1/attendance', payload)
    },
    onSuccess: async () => {
      await refetchAttendance()
    },
  })

  // ---------------- UI HANDLER ----------------
  const handleMarkAttendance = (
    employeeId: string,
    type: 'PRESENT' | 'ABSENT' | 'WORKED_DOUBLE',
  ) => {
    setPendingAttendance((prev) => {
      const existing = attendanceMap[employeeId]

      return {
        ...prev,
        [employeeId]: {
          employeeId,
          type,
          id: existing?.id,
        },
      }
    })
  }

  // ---------------- CONFIRM ALL ----------------
  const confirmAllAttendance = async () => {
    const entries = Object.values(pendingAttendance)

    try {
      await Promise.allSettled(
        entries.map((entry: any) =>
          markAttendanceMutation.mutateAsync(entry)
        )
      )

      setPendingAttendance({})
      await refetchAttendance()
    } catch (err) {
      console.error('Attendance update failed', err)
    }
  }

  // ---------------- FILTER ----------------
  const filteredEmployees = useMemo(() => {
    return employees.filter((emp) =>
      emp.fullName.toLowerCase().includes(searchQuery.toLowerCase())
    )
  }, [employees, searchQuery])

  if (empLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">Attendance</h1>
        <div className="py-8 text-center">Loading employees...</div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Attendance</h1>

        <input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="px-3 py-2 border rounded-lg"
        />
      </div>

      {/* SEARCH */}
      <input
        className="w-full px-4 py-2 border rounded-lg"
        placeholder="Search employee..."
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
      />

      {/* TABLE */}
      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-6 py-3 text-left">Employee</th>
              <th className="px-6 py-3 text-left">Wage</th>
              <th className="px-6 py-3 text-left">Status</th>
              <th className="px-6 py-3 text-center">Actions</th>
            </tr>
          </thead>

          <tbody>
            {filteredEmployees.map((emp) => {
              const attendance =
                pendingAttendance[emp.id] || attendanceMap[emp.id]

              return (
                <tr key={emp.id} className="border-b">
                  <td className="px-6 py-4">{emp.fullName}</td>

                  <td className="px-6 py-4">
                    ₹{emp.dailyWage?.toFixed(2) || '0.00'}
                  </td>

                  <td className="px-6 py-4">
                    {attendance ? (
                      <span
                        className={`px-2 py-1 rounded text-sm ${
                          attendance.type === 'PRESENT'
                            ? 'bg-green-100'
                            : attendance.type === 'ABSENT'
                              ? 'bg-red-100'
                              : 'bg-blue-100'
                        }`}
                      >
                        {attendance.type}
                      </span>
                    ) : (
                      <span className="text-gray-400">Not marked</span>
                    )}
                  </td>

                  <td className="px-6 py-4 text-center flex gap-2 justify-center">
                    <Button
                      onClick={() =>
                        handleMarkAttendance(emp.id, 'PRESENT')
                      }
                    >
                      Present
                    </Button>

                    <Button
                      onClick={() =>
                        handleMarkAttendance(emp.id, 'ABSENT')
                      }
                    >
                      Absent
                    </Button>

                    <Button
                      onClick={() =>
                        handleMarkAttendance(
                          emp.id,
                          'WORKED_DOUBLE',
                        )
                      }
                    >
                      Double
                    </Button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>

        {/* FIXED BUTTON - ALWAYS VISIBLE WHEN CHANGES EXIST */}
        {Object.keys(pendingAttendance).length > 0 && (
          <div className="fixed bottom-6 right-6 z-50 bg-white border shadow-lg rounded-xl p-4 flex items-center gap-4">
            <span className="text-sm">
              {Object.keys(pendingAttendance).length} changes
            </span>

            <Button
              onClick={confirmAllAttendance}
              className="bg-blue-600 text-white"
            >
              Update Attendance
            </Button>
          </div>
        )}
      </div>
    </div>
  )
} -->