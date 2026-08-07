# WORKETA - Complete End-to-End Testing Guide

## Overview

This guide provides step-by-step instructions to test the entire WORKETA Transport Management System backend. The system is production-ready and can be fully tested using Postman or cURL.

---

## Prerequisites

1. **Docker & Docker Compose** - For PostgreSQL database
2. **Java 21+** - To run the application
3. **Postman** - For API testing (or cURL)
4. **Maven 3.8+** - To build the project

---

## Part 1: Environment Setup

### Step 1: Start PostgreSQL using Docker Compose

```bash
cd /home/shivam/Desktop/WORKETA/backend

# Start PostgreSQL container
docker-compose up -d

# Verify container is running
docker ps | grep postgres
```

**Expected Output**: PostgreSQL container running on port 5432

### Step 2: Build the Project

```bash
# Clean and build (skip tests for faster build)
mvn clean package -DskipTests

# Expected output: BUILD SUCCESS
```

### Step 3: Run the Application

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using JAR file
java -jar target/worketa-0.0.1-SNAPSHOT.jar
```

**Expected Output**:
```
... Started WorketaApplication in X.XXX seconds (JVM running for X.XXX)
```

The backend is now running on `http://localhost:8080`

---

## Part 2: Health Check

### Test: Verify Service is Running

**Endpoint**: `GET /api/health`

**cURL**:
```bash
curl -X GET http://localhost:8080/api/health
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:8080/api/health`
- Click **Send**

**Expected Response** (200 OK):
```json
{
    "success": true,
    "message": "Service is healthy",
    "data": {
        "status": "UP",
        "message": "WORKETA Transport Management System is running",
        "version": "1.0.0",
        "timestamp": 1716380000000
    },
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

✅ **Status**: If you see this response, the backend is ready for testing.

---

## Part 3: Organisation Registration & Admin Creation

### Test 1: Register a New Organisation

**Endpoint**: `POST /api/organisations/register`

**cURL**:
```bash
curl -X POST http://localhost:8080/api/organisations/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ABC Transport Company",
    "email": "admin@abctransport.com",
    "phone": "+91-9999-999-999"
  }'
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:8080/api/organisations/register`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
    "name": "ABC Transport Company",
    "email": "admin@abctransport.com",
    "phone": "+91-9999-999-999"
}
```
- Click **Send**

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Organisation registered successfully. Default admin user created with password: TempPassword123!",
    "data": {
        "id": "uuid-here",
        "name": "ABC Transport Company",
        "email": "admin@abctransport.com",
        "phone": "+91-9999-999-999",
        "createdAt": "2026-05-22T10:00:00Z",
        "updatedAt": "2026-05-22T10:00:00Z"
    },
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

**Save the following values** for next steps:
- `organisation_id` (from response data)
- `admin_email`: `admin@abctransport.com`
- `admin_password`: `TempPassword123!`

---

## Part 4: Authentication (Login & JWT Tokens)

### Test 2: Login with Admin User

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
    "email": "admin@abctransport.com",
    "password": "TempPassword123!",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@abctransport.com",
    "password": "TempPassword123!",
    "organisationId": "INSERT-ORG-UUID-HERE"
  }'
```

**Expected Response** (200 OK):
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "uuid-refresh-token-here"
    },
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

**Save the following** for all subsequent authenticated requests:
- `accessToken` - Use in Authorization header for protected endpoints
- `refreshToken` - Use to obtain new access tokens when expired

### Test 3: Refresh Access Token

**Endpoint**: `POST /api/auth/refresh`

**Request Body**:
```json
{
    "refreshToken": "INSERT-REFRESH-TOKEN-HERE"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "INSERT-REFRESH-TOKEN-HERE"
  }'
```

**Expected Response** (200 OK):
```json
{
    "success": true,
    "message": "Token refreshed",
    "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

---

## Part 5: User Management

### Test 4: Create Additional User

**Endpoint**: `POST /api/users`

**Headers Required**:
```
Authorization: Bearer INSERT-ACCESS-TOKEN-HERE
Content-Type: application/json
```

**Request Body**:
```json
{
    "email": "operator@abctransport.com",
    "password": "SecurePassword123!",
    "fullName": "John Operator",
    "roleId": "INSERT-ADMIN-ROLE-ID-HERE",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:8080/api/users`
- Headers:
  - `Authorization: Bearer <access-token>`
  - `Content-Type: application/json`
- Body (raw JSON): See above
- Click **Send**

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "User created successfully",
    "data": {
        "id": "user-uuid",
        "email": "operator@abctransport.com",
        "fullName": "John Operator",
        "organisationId": "org-uuid",
        "active": true,
        "roles": [
            {
                "id": "role-uuid",
                "name": "ADMIN",
                "description": "Administrator"
            }
        ],
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

### Test 5: List Users

**Endpoint**: `GET /api/users`

**cURL**:
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer INSERT-ACCESS-TOKEN-HERE"
```

---

## Part 6: Employee Management

### Test 6: Create Employee

**Endpoint**: `POST /api/employees`

**Request Body**:
```json
{
    "employeeCode": "EMP001",
    "fullName": "Rajesh Kumar",
    "type": "DRIVER",
    "joiningDate": "2026-01-15",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer INSERT-ACCESS-TOKEN-HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "EMP001",
    "fullName": "Rajesh Kumar",
    "type": "DRIVER",
    "joiningDate": "2026-01-15",
    "organisationId": "INSERT-ORG-UUID-HERE"
  }'
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Employee created successfully",
    "data": {
        "id": "emp-uuid",
        "employeeCode": "EMP001",
        "fullName": "Rajesh Kumar",
        "type": "DRIVER",
        "joiningDate": "2026-01-15",
        "active": true,
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

**Save**: `employee_id` for Rajesh Kumar

### Test 7: Create Second Employee (Assistant)

```json
{
    "employeeCode": "EMP002",
    "fullName": "Suresh Singh",
    "type": "ASSISTANT",
    "joiningDate": "2026-02-10",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Save**: `employee_id` for Suresh Singh

### Test 8: List Employees

**Endpoint**: `GET /api/employees`

**cURL**:
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer INSERT-ACCESS-TOKEN-HERE"
```

---

## Part 7: Vehicle Management

### Test 9: Create Vehicle

**Endpoint**: `POST /api/vehicles`

**Request Body**:
```json
{
    "vehicleNumber": "DL-01-AB-1234",
    "type": "TRUCK",
    "capacity": 15000,
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer INSERT-ACCESS-TOKEN-HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleNumber": "DL-01-AB-1234",
    "type": "TRUCK",
    "capacity": 15000,
    "organisationId": "INSERT-ORG-UUID-HERE"
  }'
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Vehicle created successfully",
    "data": {
        "id": "vehicle-uuid",
        "vehicleNumber": "DL-01-AB-1234",
        "type": "TRUCK",
        "capacity": 15000,
        "active": true,
        "activeFrom": "2026-05-22",
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

**Save**: `vehicle_id`

### Test 10: Create Second Vehicle

```json
{
    "vehicleNumber": "DL-01-CD-5678",
    "type": "VAN",
    "capacity": 8000,
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

---

## Part 8: Company/Client Management

### Test 11: Create Company

**Endpoint**: `POST /api/companies`

**Request Body**:
```json
{
    "name": "Walmart Logistics",
    "contactPerson": "Amit Sharma",
    "phone": "+91-8888-777-666",
    "email": "contact@walmart-logistics.com",
    "address": "123 Business Park, New Delhi",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Company created successfully",
    "data": {
        "id": "company-uuid",
        "name": "Walmart Logistics",
        "contactPerson": "Amit Sharma",
        "phone": "+91-8888-777-666",
        "email": "contact@walmart-logistics.com",
        "address": "123 Business Park, New Delhi",
        "active": true,
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

**Save**: `company_id`

---

## Part 9: Trip Management (Core Business Logic)

### Test 12: Create Trip (Overlap Validation Test)

**Endpoint**: `POST /api/trips`

**Request Body**:
```json
{
    "vehicleId": "INSERT-VEHICLE-UUID-HERE",
    "driverId": "INSERT-DRIVER-UUID-HERE",
    "assistantId": "INSERT-ASSISTANT-UUID-HERE",
    "companyId": "INSERT-COMPANY-UUID-HERE",
    "route": "Delhi to Jaipur",
    "tripDate": "2026-05-25",
    "startTime": "09:00:00",
    "endTime": "15:30:00",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer INSERT-ACCESS-TOKEN-HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": "INSERT-VEHICLE-UUID-HERE",
    "driverId": "INSERT-DRIVER-UUID-HERE",
    "assistantId": "INSERT-ASSISTANT-UUID-HERE",
    "companyId": "INSERT-COMPANY-UUID-HERE",
    "route": "Delhi to Jaipur",
    "tripDate": "2026-05-25",
    "startTime": "09:00:00",
    "endTime": "15:30:00",
    "organisationId": "INSERT-ORG-UUID-HERE"
  }'
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Trip created successfully",
    "data": {
        "id": "trip-uuid",
        "vehicleId": "vehicle-uuid",
        "driverId": "driver-uuid",
        "assistantId": "assistant-uuid",
        "companyId": "company-uuid",
        "route": "Delhi to Jaipur",
        "tripDate": "2026-05-25",
        "startTime": "09:00:00",
        "endTime": "15:30:00",
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

**Save**: `trip_id`

### Test 13: Test Overlap Validation (Negative Test)

Try to create a trip with the same vehicle and overlapping time:

**Request Body**:
```json
{
    "vehicleId": "INSERT-VEHICLE-UUID-HERE",
    "driverId": "INSERT-DIFFERENT-DRIVER-UUID-HERE",
    "assistantId": "INSERT-ASSISTANT-UUID-HERE",
    "companyId": "INSERT-COMPANY-UUID-HERE",
    "route": "Delhi to Agra",
    "tripDate": "2026-05-25",
    "startTime": "14:00:00",
    "endTime": "18:00:00",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Expected Response** (400 Bad Request):
```json
{
    "success": false,
    "message": "Vehicle has overlapping trip on this date",
    "data": null,
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

✅ **Validation Success**: This proves the trip overlap validation is working correctly.

### Test 14: List Trips

**Endpoint**: `GET /api/trips`

**cURL**:
```bash
curl -X GET http://localhost:8080/api/trips \
  -H "Authorization: Bearer INSERT-ACCESS-TOKEN-HERE"
```

---

## Part 10: Attendance Management

### Test 15: Mark Attendance

**Endpoint**: `POST /api/attendance`

**Request Body**:
```json
{
    "employeeId": "INSERT-EMPLOYEE-UUID-HERE",
    "attendanceDate": "2026-05-22",
    "present": true,
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Attendance marked successfully",
    "data": {
        "id": "attendance-uuid",
        "employeeId": "emp-uuid",
        "attendanceDate": "2026-05-22",
        "present": true,
        "checkinTime": null,
        "checkoutTime": null,
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

---

## Part 11: Employee Advance Management

### Test 16: Create Employee Advance

**Endpoint**: `POST /api/advances`

**Request Body**:
```json
{
    "employeeId": "INSERT-EMPLOYEE-UUID-HERE",
    "amount": 5000,
    "advanceDate": "2026-05-22",
    "note": "Salary advance for May",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Advance created successfully",
    "data": {
        "id": "advance-uuid",
        "employeeId": "emp-uuid",
        "amount": 5000,
        "advanceDate": "2026-05-22",
        "note": "Salary advance for May",
        "settled": false,
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

---

## Part 12: Payroll Management

### Test 17: Create Payroll Settlement

**Endpoint**: `POST /api/payroll`

**Request Body**:
```json
{
    "employeeId": "INSERT-EMPLOYEE-UUID-HERE",
    "month": "2026-05",
    "baseSalary": 25000,
    "advances": 5000,
    "remarks": "May 2026 payroll settlement",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Expected Response** (201 Created):
```json
{
    "success": true,
    "message": "Payroll created successfully",
    "data": {
        "id": "payroll-uuid",
        "employeeId": "emp-uuid",
        "month": "2026-05",
        "baseSalary": 25000,
        "advances": 5000,
        "netAmount": 20000,
        "remarks": "May 2026 payroll settlement",
        "organisationId": "org-uuid",
        "createdAt": "2026-05-22T10:00:00Z"
    }
}
```

---

## Part 13: Testing Error Scenarios

### Test 18: Duplicate Email Registration

Try to register another organisation with the same email:

**Endpoint**: `POST /api/organisations/register`

**Request Body**:
```json
{
    "name": "Another Company",
    "email": "admin@abctransport.com",
    "phone": "+91-1234-567-890"
}
```

**Expected Response** (400 Bad Request):
```json
{
    "success": false,
    "message": "Organisation with this email already exists",
    "data": null,
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

### Test 19: Invalid Login Credentials

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
    "email": "admin@abctransport.com",
    "password": "WrongPassword123!",
    "organisationId": "INSERT-ORG-UUID-HERE"
}
```

**Expected Response** (400 Bad Request):
```json
{
    "success": false,
    "message": "Invalid credentials",
    "data": null,
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

### Test 20: Missing Authorization Header

Try to access a protected endpoint without token:

**Endpoint**: `GET /api/employees`

**cURL** (without Authorization header):
```bash
curl -X GET http://localhost:8080/api/employees
```

**Expected Response** (401 Unauthorized):
```json
{
    "success": false,
    "message": "Unauthorized: Missing or invalid authentication token",
    "data": null,
    "timestamp": "2026-05-22T10:00:00.000Z"
}
```

---

## Part 14: Database Verification

### Verify Data Persistence

Connect to PostgreSQL to verify data is stored correctly:

```bash
# Connect to PostgreSQL
docker exec -it worketa-db psql -U worketa -d worketa

# Inside psql terminal:
# List all organisations
SELECT id, name, email FROM organisations;

# List all users
SELECT id, email, full_name FROM users;

# List all employees
SELECT id, employee_code, full_name, type FROM employees;

# List all vehicles
SELECT id, vehicle_number, type, capacity FROM vehicles;

# List all trips
SELECT id, vehicle_id, driver_id, route, trip_date FROM trips;

# Exit psql
\q
```

---

## Part 15: Running Unit Tests

### Execute All Tests

```bash
cd /home/shivam/Desktop/WORKETA/backend

# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=OrganisationServiceTest

# Run with coverage report
mvn test jacoco:report
```

### View Test Results

```bash
# Maven test report
cat target/surefire-reports/*.txt

# Coverage report (if using Jacoco)
open target/site/jacoco/index.html
```

---

## Complete Test Checklist

Use this checklist to verify all functionality:

### Authentication & Security ✓
- [ ] Health endpoint returns UP status
- [ ] Organisation registration succeeds
- [ ] Default admin user is created
- [ ] Login with correct credentials succeeds
- [ ] Login with incorrect credentials fails
- [ ] Refresh token generates new access token
- [ ] Missing token on protected endpoint fails

### Organisation & Users ✓
- [ ] Register multiple organisations
- [ ] Create additional users with roles
- [ ] List users returns correct data
- [ ] Users are scoped to their organisation

### Employees ✓
- [ ] Create employee with valid joining date
- [ ] Create employee with future joining date fails
- [ ] List employees shows only org's employees
- [ ] Get employee by ID works correctly

### Vehicles ✓
- [ ] Create vehicle with unique number
- [ ] Duplicate vehicle number fails
- [ ] List vehicles shows only org's vehicles
- [ ] Get vehicle by ID works correctly

### Trips ✓
- [ ] Create trip with valid data succeeds
- [ ] Trip with vehicle overlap fails
- [ ] Trip with driver conflict fails
- [ ] List trips returns correct data
- [ ] Pagination works correctly

### Attendance ✓
- [ ] Mark attendance for employee
- [ ] List attendance records
- [ ] Duplicate attendance date fails

### Advances ✓
- [ ] Create employee advance
- [ ] List advances by organisation
- [ ] Settled flag works correctly

### Payroll ✓
- [ ] Create payroll settlement
- [ ] Net amount calculated correctly (baseSalary - advances)
- [ ] Duplicate payroll month fails
- [ ] List payroll records

---

## Troubleshooting

### Issue: Database Connection Refused

**Solution**:
```bash
# Check if PostgreSQL container is running
docker ps | grep postgres

# Restart container
docker-compose down
docker-compose up -d
```

### Issue: Build Fails with Compilation Errors

**Solution**:
```bash
# Clean and rebuild
mvn clean
mvn compile
mvn package -DskipTests
```

### Issue: Port 8080 Already in Use

**Solution**:
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or run on different port
java -jar target/worketa-0.0.1-SNAPSHOT.jar --server.port=8081
```

### Issue: JWT Token Expired

**Solution**: Use the refresh token endpoint to get a new access token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "INSERT-REFRESH-TOKEN-HERE"}'
```

---

## Summary

You now have a **production-ready Transport Management System** with:

✅ Multi-tenant organisation isolation  
✅ JWT-based authentication  
✅ 11 business modules fully implemented  
✅ Comprehensive validation and error handling  
✅ Transactional consistency  
✅ PostgreSQL database with Flyway migrations  
✅ Health endpoint for monitoring  
✅ Unit tests for core services  

**Status**: Ready for production deployment 🚀
