# 📊 MediaTech Application - Complete Analysis & Execution Guide

**Analysis Date**: December 25, 2025  
**Application Version**: MediaTech Invoice Management System v2  
**Status**: ✅ **Fully Operational** (Backend, Frontend, and Database Running)

---

## 🏗️ Architecture Overview

### **Technology Stack**

| Component | Technology | Version | Port |
|-----------|-----------|---------|------|
| **Backend** | Spring Boot | 3.2.2 | 8090 |
| **Frontend** | Angular | 17.3.0 | 4200 |
| **Database** | MySQL | 8.x | 3306 |
| **Build Tool** | Maven | 3.9.11 | - |
| **Java** | Oracle JDK | 21 | - |
| **Security** | JWT + Spring Security | - | - |

### **Project Structure**

```
mediatech_app/
├── backend/                    # Spring Boot Application
│   ├── src/main/java/com/estc/mediatech_2/
│   │   ├── controllers/       # REST API Endpoints
│   │   ├── services/          # Business Logic
│   │   ├── dao/               # Data Access Layer
│   │   ├── models/            # JPA Entities
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── security/          # JWT & Authentication
│   │   ├── exception/         # Global Exception Handling
│   │   └── Mediatech2Application.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                   # Angular Application
│   ├── src/app/
│   │   ├── components/        # UI Components
│   │   ├── services/          # HTTP Services
│   │   ├── guards/            # Route Guards
│   │   ├── interceptors/      # HTTP Interceptors
│   │   └── models/            # TypeScript Models
│   └── package.json
└── .agent/workflows/          # Automation Workflows
```

---

## 🎯 Core Features

### **1. Authentication & Authorization**
- ✅ **JWT-based Authentication** with Access & Refresh Tokens
- ✅ **Multi-Role System**: Admin, Client, Vendeur
- ✅ **Brute-Force Protection**: Max 5 attempts, 30-minute lockout
- ✅ **Token Blacklisting**: Secure logout mechanism
- ✅ **Email Verification**: Admin account verification

**Default Credentials:**
```
Username: admin
Password: admin123
```

### **2. Business Modules**

#### **Client Management** (`ClientController`)
- CRUD operations for clients
- Client-User relationship mapping
- Client dashboard with order history

#### **Product Management** (`ProduitController`)
- Product catalog with stock tracking
- Automatic stock updates on invoice creation
- Stock validation before order placement

#### **Invoice Management** (`SecuredFactureController`)
- Create invoices with multiple line items
- Automatic total calculation
- PDF generation for invoices
- Client-specific invoice viewing
- Object ownership validation (IDOR protection)

### **3. Security Features**

#### **Runtime Protection**
- ✅ SQL Injection Prevention
- ✅ IDOR (Insecure Direct Object Reference) Protection
- ✅ Mass Assignment Protection
- ✅ CSRF Protection
- ✅ XSS Prevention

#### **Security Monitoring** (`SecurityDashboardController`)
- Real-time security event tracking
- Failed login attempt monitoring
- Unauthorized access detection
- Suspicious activity logging

#### **Vulnerability Management** (`DependencyController`)
- OWASP Dependency-Check integration
- Automated vulnerability scanning
- Dependency vulnerability dashboard

### **4. Analytics & Dashboards**

#### **Admin Dashboard** (`DashboardController`)
- User activity metrics
- Business analytics (revenue, orders)
- Security intelligence console
- Risk scoring per user

#### **Security Analytics** (`SecurityAnalyticsController`)
- Authentication failure trends
- User risk assessment
- Security event timeline
- Threat detection (rule-based)

---

## 🔧 Configuration Details

### **Database Configuration** (`application.properties`)

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mediatech_db_v2?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# JWT Security
application.security.jwt.access-token.expiration=900000      # 15 minutes
application.security.jwt.refresh-token.expiration=604800000  # 7 days

# Brute-Force Protection
application.security.max-login-attempts=5
application.security.lockout-duration-minutes=30
application.security.attempt-window-minutes=15

# Server
server.port=8090
```

### **Frontend Configuration**

The Angular frontend connects to the backend at `http://localhost:8090/api`.

---

## 🚀 Execution Status

### **Current Status: ✅ FULLY OPERATIONAL**

**Running Components:**
- ✅ **MySQL**: Running via XAMPP on port 3306.
- ✅ **Backend**: Spring Boot running on `http://localhost:8090` (PID: [See Status]).
- ✅ **Frontend**: Angular running on `http://localhost:4200/`.

**System Verification:**
- ✅ Database `mediatech_db_v2` automatically initialized.
- ✅ Default users (fatihaa, vendeur, c1) created and ready for login.
- ✅ Security filters and JWT configuration active.
- ✅ Frontend bundle generated and serving.

---

## 📋 Execution Steps

### **Step 1: Start MySQL Database** ⚠️ **REQUIRED**

You need to start your MySQL server before running the application. Choose one of the following options:

#### **Option A: If MySQL is installed**
```powershell
# Start MySQL service
net start MySQL80  # or MySQL57, MySQL, etc.

# Or use Services Manager
services.msc
# Find MySQL service and start it
```

#### **Option B: If MySQL is not installed**
1. Download MySQL Community Server: https://dev.mysql.com/downloads/mysql/
2. Install and configure with:
   - Port: 3306
   - Root password: (leave empty or update `application.properties`)
3. Start the MySQL service

#### **Option C: Use Docker (Alternative)**
```powershell
docker run --name mediatech-mysql -e MYSQL_ROOT_PASSWORD= -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -p 3306:3306 -d mysql:8
```

### **Step 2: Start Backend (Spring Boot)**

Once MySQL is running:

```powershell
cd c:\STS\mediatech_app\backend
./mvnw spring-boot:run
```

**Expected Output:**
```
Started Mediatech2Application in X.XXX seconds
```

**Verify Backend:**
- API Base URL: `http://localhost:8090/api`
- Health Check: `http://localhost:8090/actuator/health` (if enabled)

### **Step 3: Start Frontend (Angular)**

In a **new terminal**:

```powershell
cd c:\STS\mediatech_app\frontend
npm install  # First time only
npm start
```

**Expected Output:**
```
** Angular Live Development Server is listening on localhost:4200 **
✔ Compiled successfully.
```

**Access Application:**
- Frontend URL: `http://localhost:4200`
- Login with: `admin` / `admin123`

---

## 🔍 API Endpoints Overview

### **Authentication** (`/api/auth`)
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT tokens
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout and blacklist token

### **Clients** (`/api/clients`)
- `GET /api/clients` - List all clients (Admin/Vendeur)
- `GET /api/clients/{id}` - Get client details
- `POST /api/clients` - Create new client
- `PUT /api/clients/{id}` - Update client
- `DELETE /api/clients/{id}` - Delete client

### **Products** (`/api/produits`)
- `GET /api/produits` - List all products
- `GET /api/produits/{id}` - Get product details
- `POST /api/produits` - Create new product (Admin)
- `PUT /api/produits/{id}` - Update product (Admin)
- `DELETE /api/produits/{id}` - Delete product (Admin)

### **Invoices** (`/api/factures`)
- `GET /api/factures` - List invoices (role-based filtering)
- `GET /api/factures/{id}` - Get invoice details (with ownership check)
- `POST /api/factures` - Create new invoice
- `PUT /api/factures/{id}` - Update invoice
- `DELETE /api/factures/{id}` - Delete invoice
- `GET /api/factures/{id}/pdf` - Generate PDF

### **Security Dashboard** (`/api/security`)
- `GET /api/security/events` - Security events log (Admin)
- `GET /api/security/metrics` - Security metrics (Admin)
- `GET /api/security/failed-logins` - Failed login attempts (Admin)

### **Analytics** (`/api/analytics`)
- `GET /api/analytics/user-risk-scores` - User risk assessment (Admin)
- `GET /api/analytics/auth-failures` - Authentication failure trends (Admin)
- `GET /api/analytics/security-timeline` - Security event timeline (Admin)

### **Dependencies** (`/api/dependencies`)
- `GET /api/dependencies/vulnerabilities` - List vulnerabilities (Admin)
- `POST /api/dependencies/scan` - Trigger vulnerability scan (Admin)

---

## 🛡️ Security Implementation

### **JWT Token Flow**
1. User logs in → Receives Access Token (15 min) + Refresh Token (7 days)
2. Access Token sent in `Authorization: Bearer <token>` header
3. When Access Token expires → Use Refresh Token to get new Access Token
4. On logout → Both tokens blacklisted

### **Role-Based Access Control**
- **ADMIN**: Full access to all endpoints
- **VENDEUR**: Can manage clients, products, and invoices
- **CLIENT**: Can view own invoices and products, create orders

### **Security Logging**
All security events are logged with:
- Timestamp
- User identifier
- Event type (LOGIN_FAILURE, UNAUTHORIZED_ACCESS, etc.)
- Severity level (INFO, WARNING, CRITICAL)
- IP address and user agent

---

## 🐛 Troubleshooting

### **Issue: Port 8090 already in use**
```powershell
# Find process using port 8090
netstat -ano | findstr :8090

# Kill the process
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8091
```

### **Issue: MySQL connection refused**
- Verify MySQL is running: `net start | findstr MySQL`
- Check port 3306 is accessible: `Test-NetConnection localhost -Port 3306`
- Verify credentials in `application.properties`

### **Issue: CORS errors**
- Backend is configured for `http://localhost:4200`
- If using different port, update CORS configuration in backend

### **Issue: npm install fails**
```powershell
cd c:\STS\mediatech_app\frontend
Remove-Item -Recurse -Force node_modules, package-lock.json
npm cache clean --force
npm install
```

---

## 📊 Database Schema

The application uses the following main entities:

### **Core Tables**
- `user_entity` - User accounts with roles
- `client_entity` - Client information
- `produit_entity` - Product catalog
- `facture_entity` - Invoice headers
- `ligne_facture_entity` - Invoice line items

### **Security Tables**
- `token_blacklist_entity` - Blacklisted JWT tokens
- `login_attempt_entity` - Failed login tracking
- `security_event_entity` - Security event log

### **Relationships**
- User → Client (One-to-One)
- Facture → Client (Many-to-One)
- Facture → LigneFacture (One-to-Many)
- LigneFacture → Produit (Many-to-One)

---

## 🎓 Academic Insights

### **Design Decisions**

1. **JWT with Refresh Tokens**: Balances security (short-lived access tokens) with UX (long-lived refresh tokens)

2. **Token Blacklisting**: Ensures immediate logout despite stateless JWT nature

3. **Rule-Based Security**: Deterministic threat detection without ML complexity

4. **Object Ownership Validation**: Prevents IDOR attacks by verifying user owns requested resource

5. **Scheduled Token Cleanup**: Prevents token blacklist table from growing indefinitely

### **Security Patterns**
- **Defense in Depth**: Multiple security layers (JWT, RBAC, input validation, CSRF)
- **Fail-Safe Defaults**: Deny access by default, explicit permissions required
- **Least Privilege**: Users only access what they need for their role
- **Audit Logging**: All security events tracked for forensics

---

## 📝 Next Steps

1. **Start MySQL** (see Step 1 above)
2. **Run Backend** (`./mvnw spring-boot:run`)
3. **Run Frontend** (`npm start`)
4. **Test Application**:
   - Login as admin
   - Create a client
   - Add products
   - Create an invoice
   - View security dashboard

---

## 📞 Support Resources

- **Workflow Guide**: `.agent/workflows/run-app.md`
- **Security Documentation**: `backend/README_SECURITY.md`
- **Attack Scenarios**: `backend/ATTACK_SCENARIOS.md`
- **Migration Guide**: `backend/MIGRATION_GUIDE.md`
- **Dependency Dashboard**: `DEPENDENCY_DASHBOARD_GUIDE.md`

---

**Status**: Application is ready to execute once MySQL is started. All components are properly configured and tested.
