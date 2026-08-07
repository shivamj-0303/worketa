# WORKETA - Quick Start Guide

## 🚀 Start in 5 Minutes

### 1. Start PostgreSQL

```bash
cd /home/shivam/Desktop/WORKETA/backend
docker-compose up -d
```

### 2. Build & Run

```bash
mvn clean package -DskipTests
java -jar target/worketa-0.0.1-SNAPSHOT.jar
```

### 3. Verify Health

```bash
curl http://localhost:8080/api/health
```

✅ You should see: `"status": "UP"`

---

## 📋 Testing Workflow (Copy-Paste Ready)

### Step 1: Register Organisation

```bash
curl -X POST http://localhost:8080/api/organisations/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Transport Company",
    "email": "admin@mytransport.com",
    "phone": "+91-9876543210"
  }'
```

**Save from response:**
- `id` → Use as `ORG_ID`
- Admin password: `TempPassword123!`

### Step 2: Login

Replace `ORG_ID` in the command:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@mytransport.com",
    "password": "TempPassword123!",
    "organisationId": "ORG_ID"
  }'
```

**Save from response:**
- `accessToken` → Use as `ACCESS_TOKEN` in all requests below
- `refreshToken` → Keep for refreshing token

### Step 3: Create Employee (Driver)

Replace `ORG_ID` and `ACCESS_TOKEN`:

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "DRIVER001",
    "fullName": "Rajesh Kumar",
    "type": "DRIVER",
    "joiningDate": "2026-01-15",
    "organisationId": "ORG_ID"
  }'
```

**Save:** `id` → Use as `DRIVER_ID`

### Step 4: Create Employee (Assistant)

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "ASSISTANT001",
    "fullName": "Suresh Singh",
    "type": "ASSISTANT",
    "joiningDate": "2026-02-10",
    "organisationId": "ORG_ID"
  }'
```

**Save:** `id` → Use as `ASSISTANT_ID`

### Step 5: Create Vehicle

```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleNumber": "DL-01-AB-1234",
    "type": "TRUCK",
    "capacity": 15000,
    "organisationId": "ORG_ID"
  }'
```

**Save:** `id` → Use as `VEHICLE_ID`

### Step 6: Create Company

```bash
curl -X POST http://localhost:8080/api/companies \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Walmart Logistics",
    "contactPerson": "Amit Sharma",
    "phone": "+91-8888-777-666",
    "email": "contact@walmart.com",
    "address": "Delhi",
    "organisationId": "ORG_ID"
  }'
```

**Save:** `id` → Use as `COMPANY_ID`

### Step 7: Create Trip (Core Business Logic)

```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": "VEHICLE_ID",
    "driverId": "DRIVER_ID",
    "assistantId": "ASSISTANT_ID",
    "companyId": "COMPANY_ID",
    "route": "Delhi to Jaipur",
    "tripDate": "2026-05-25",
    "startTime": "09:00:00",
    "endTime": "15:30:00",
    "organisationId": "ORG_ID"
  }'
```

**Save:** `id` → Use as `TRIP_ID`

### Step 8: Test Trip Overlap Validation (Try to create overlapping trip)

```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": "VEHICLE_ID",
    "driverId": "DRIVER_ID",
    "assistantId": "ASSISTANT_ID",
    "companyId": "COMPANY_ID",
    "route": "Delhi to Agra",
    "tripDate": "2026-05-25",
    "startTime": "14:00:00",
    "endTime": "18:00:00",
    "organisationId": "ORG_ID"
  }'
```

**Expected:** ❌ Error: `"Vehicle has overlapping trip"`

### Step 9: Mark Attendance

```bash
curl -X POST http://localhost:8080/api/attendance \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "DRIVER_ID",
    "attendanceDate": "2026-05-22",
    "present": true,
    "organisationId": "ORG_ID"
  }'
```

### Step 10: Create Employee Advance

```bash
curl -X POST http://localhost:8080/api/advances \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "DRIVER_ID",
    "amount": 5000,
    "advanceDate": "2026-05-22",
    "note": "May salary advance",
    "organisationId": "ORG_ID"
  }'
```

### Step 11: Create Payroll Settlement

```bash
curl -X POST http://localhost:8080/api/payroll \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "DRIVER_ID",
    "month": "2026-05",
    "baseSalary": 25000,
    "advances": 5000,
    "remarks": "May payroll",
    "organisationId": "ORG_ID"
  }'
```

**Expected:** Net amount = 25000 - 5000 = 20000

### Step 12: List All Resources

```bash
# List employees
curl http://localhost:8080/api/employees \
  -H "Authorization: Bearer ACCESS_TOKEN"

# List vehicles
curl http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer ACCESS_TOKEN"

# List trips
curl http://localhost:8080/api/trips \
  -H "Authorization: Bearer ACCESS_TOKEN"

# List attendance
curl http://localhost:8080/api/attendance \
  -H "Authorization: Bearer ACCESS_TOKEN"

# List advances
curl http://localhost:8080/api/advances \
  -H "Authorization: Bearer ACCESS_TOKEN"

# List payroll
curl http://localhost:8080/api/payroll \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

---

## 🔧 Use Postman for Easier Testing

### Import Collection

1. Open Postman
2. Click **Import** → **Upload Files**
3. Select `WORKETA_Postman_Collection.json`
4. Update variables in **Environments**:
   - `BASE_URL`: `http://localhost:8080`
   - `ORG_ID`: From Step 1
   - `ACCESS_TOKEN`: From Step 2
   - `DRIVER_ID`, `VEHICLE_ID`, etc.: From respective create calls

---

## 📊 Database Verification

Connect to PostgreSQL to verify data:

```bash
docker exec -it worketa-db psql -U worketa -d worketa

# Inside psql:
SELECT * FROM organisations;
SELECT * FROM employees;
SELECT * FROM vehicles;
SELECT * FROM trips;
SELECT * FROM attendance;
SELECT * FROM advances;
SELECT * FROM payroll;

# Exit
\q
```

---

## 🧪 Run Unit Tests

```bash
mvn test
```

---

## 📁 API Endpoints Summary

| Module | Endpoint | Method |
|--------|----------|--------|
| Health | `/api/health` | GET |
| Organisation | `/api/organisations/register` | POST |
| Auth | `/api/auth/login` | POST |
| Auth | `/api/auth/refresh` | POST |
| Users | `/api/users` | POST, GET |
| Employees | `/api/employees` | POST, GET, DELETE |
| Vehicles | `/api/vehicles` | POST, GET, DELETE |
| Companies | `/api/companies` | POST, GET, PUT, DELETE |
| Trips | `/api/trips` | POST, GET |
| Attendance | `/api/attendance` | POST, GET |
| Advances | `/api/advances` | POST, GET |
| Payroll | `/api/payroll` | POST, GET |

---

## ✅ System Features Included

✅ **Multi-Tenant** - Data isolated by organisation  
✅ **JWT Auth** - Secure token-based authentication  
✅ **Role-Based Access** - ADMIN, OPERATOR, ACCOUNTANT roles  
✅ **Trip Overlap Detection** - Prevents vehicle/driver conflicts  
✅ **Soft Delete** - Employees and vehicles marked inactive instead of deleted  
✅ **Wage History** - Track employee salary changes  
✅ **Payroll Calculation** - Automatic deduction of advances  
✅ **Flyway Migrations** - Versioned database schema  
✅ **PostgreSQL** - Production-grade database  
✅ **Exception Handling** - Global error responses  
✅ **Unit Tests** - Service layer tested  

---

## 🛠️ Troubleshooting

**Port 8080 in use?**
```bash
lsof -ti:8080 | xargs kill -9
```

**Database connection error?**
```bash
docker-compose down
docker-compose up -d
```

**Build failure?**
```bash
mvn clean compile
```

**JWT expired?**
```bash
# Use refresh token to get new access token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

---

## 📞 Support

All endpoints return standardized responses:

**Success (200):**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ },
  "timestamp": "2026-05-22T10:00:00.000Z"
}
```

**Error (4xx/5xx):**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-05-22T10:00:00.000Z"
}
```

---

**Status**: ✅ Production Ready  
**Version**: 1.0.0  
**Last Updated**: May 22, 2026
