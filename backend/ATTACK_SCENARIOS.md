# 🛡️ Attack Scenarios & Defense Mechanisms

## **STEP 2: Security Vulnerability Detection Documentation**

This document provides real-world attack scenarios and explains how the MediaTech application blocks each attack.

---

## 📋 **Table of Contents**

1. [SQL Injection](#1-sql-injection)
2. [IDOR (Insecure Direct Object Reference)](#2-idor-attack)
3. [Mass Assignment](#3-mass-assignment)
4. [Authentication Attacks](#4-authentication-attacks)
5. [Session Hijacking](#5-session-hijacking)

---

## **1. SQL Injection**

### **OWASP Reference:** A03:2021 – Injection

### **Attack Scenario:**

```bash
# Attacker tries to bypass login with SQL injection
POST /api/auth/login
{
  "username": "admin' OR '1'='1",
  "password": "anything"
}

# Another attempt - Union-based injection
POST /api/clients/search
{
  "name": "John' UNION SELECT password FROM users--"
}

# Time-based blind SQL injection
GET /api/factures?id=1; WAITFOR DELAY '00:00:10'--
```

### **How MediaTech Blocks It:**

#### **Primary Defense: Parameterized Queries**
```java
// ✅ SAFE - Uses JPA with parameterized queries
@Query("SELECT u FROM UserEntity u WHERE u.username = :username")
Optional<UserEntity> findByUsername(@Param("username") String username);

// Spring Data JPA generates parameterized queries automatically
// The username is passed as a parameter, NOT concatenated into SQL
```

#### **Secondary Defense: Input Validation**
```java
// SQLInjectionValidator detects malicious patterns
public void validateInput(String fieldName, String input) {
    // Detects patterns like:
    // - OR '1'='1
    // - UNION SELECT
    // - DROP TABLE
    // - EXEC, WAITFOR, SLEEP
    // - SQL comments (--,  /*, */)
    
    if (pattern.matcher(input).find()) {
        throw new PotentialSQLInjectionException(fieldName, input);
    }
}
```

#### **Tertiary Defense: Exception Handling**
```java
// SecurityExceptionHandler catches and logs the attempt
@ExceptionHandler(PotentialSQLInjectionException.class)
public ResponseEntity<?> handleSQLInjection(...) {
    // Log critical incident
    log.error("🚨 SQL INJECTION ATTEMPT DETECTED");
    auditService.logCriticalSecurityIncident(...);
    
    // Return generic error (don't reveal what we detected)
    return ResponseEntity.badRequest()
        .body("Invalid input");
}
```

### **What Happens:**

1. ✅ **Input Validation** - Pattern detected immediately
2. ✅ **Exception Thrown** - `PotentialSQLInjectionException`
3. ✅ **Logged** - Event saved to `security_events` table with CRITICAL severity
4. ✅ **Response** - Generic error message (no information disclosure)
5. ✅ **Audit Trail** - Full details logged for forensic analysis

### **Forensic Log Example:**
```
🚨🚨🚨 SQL INJECTION ATTEMPT DETECTED - User: anonymous, 
Input: admin' OR '1'='1, IP: 192.168.1.100
```

---

## **2. IDOR Attack**

### **OWASP Reference:** A01:2021 – Broken Access Control

### **Attack Scenario:**

```bash
# Attacker is logged in as CLIENT (owns facture ID 456)
# Tries to access another client's facture

GET /api/factures/123
Authorization: Bearer <client_token>

# Another attempt - Modify another client's data
PUT /api/clients/789
{ "telephone": "0600000000" }
```

### **How MediaTech Blocks It:**

#### **Primary Defense: Ownership Validation**
```java
// SecuredFactureController.java
@GetMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ADMIN', 'VENDEUR', 'CLIENT')")
public ResponseEntity<?> getFacture(@PathVariable Long id) {
    FactureResponseDto facture = factureService.getFacture(id);
    
    // Authorization check: verify user can access this facture
    if (!canAccessFacture(auth, facture)) {
        log.warn("IDOR attempt: {} tried to access facture {}", 
                 auth.getName(), id);
        throw new InsecureDirectObjectReferenceException(...);
    }
    
    return ResponseEntity.ok(facture);
}

private boolean canAccessFacture(Authentication auth, FactureResponseDto facture) {
    // ADMIN and VENDEUR: Can access all
    if (hasAdminOrVendeurRole(auth)) {
        return true;
    }
    
    // CLIENT: Can only access their own factures
    return factureOwnership.belongsToClient(auth.getName(), facture);
}
```

#### **Secondary Defense: IDOR Validator**
```java
// IDORValidator validates ownership explicitly
idorValidator.validateFactureAccess(
    factureOwnerUsername, 
    requestedBy, 
    userRole
);

// If validation fails:
throw new InsecureDirectObjectReferenceException(username, resource);
```

### **What Happens:**

1. ✅ **Request Intercepted** - JWT filter validates token
2. ✅ **Authorization Check** - `canAccessFacture()` verifies ownership
3. ✅ **IDOR Detected** - User doesn't own the resource
4. ✅ **Exception Thrown** - `InsecureDirectObjectReferenceException`
5. ✅ **Logged** - CRITICAL event with full details
6. ✅ **Response** - 403 Forbidden (no information disclosure)

### **Forensic Log Example:**
```
🚨🚨 IDOR ATTACK DETECTED - User: client_john, 
Resource: /api/factures/123, AttemptedAccess: facture/123, 
IP: 192.168.1.100
```

---

## **3. Mass Assignment**

### **OWASP Reference:** A04:2021 – Insecure Design

### **Attack Scenario:**

```bash
# Attacker tries to escalate privileges by modifying role
PUT /api/users/profile
{
  "username": "attacker",
  "email": "attacker@example.com",
  "role": "ADMIN"  ← Attempting privilege escalation
}

# Another attempt - Enable locked account
PUT /api/users/profile
{
  "username": "attacker",
  "accountLocked": false,
  "enabled": true
}
```

### **How MediaTech Blocks It:**

#### **Primary Defense: DTO Design**
```java
// ✅ SAFE - DTO only includes allowed fields
public class UserUpdateDto {
    private String email;
    private String telephone;
    // NO role field
    // NO enabled field
    // NO accountLocked field
}

// Controller uses DTO instead of Entity
@PutMapping("/profile")
public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDto dto) {
    // Only email and telephone can be updated
    // Protected fields are ignored
}
```

#### **Secondary Defense: Mass Assignment Validator**
```java
// Validate that no protected fields are being modified
massAssignmentValidator.validateUserUpdate(fieldsBeingUpdated);

// Protected fields list:
// - id, id_user
// - role
// - enabled
// - accountLocked
// - lockoutTime
// - failedAttempts
```

#### **Tertiary Defense: Role Validation**
```java
// If role is somehow being modified, validate it
massAssignmentValidator.validateRoleModification(currentRole, newRole);

// Only ADMIN can change roles
if (!currentRole.equals(newRole) && !isAdmin()) {
    throw new MassAssignmentException(username, "role");
}
```

### **What Happens:**

1. ✅ **DTO Binding** - Only allowed fields are bound
2. ✅ **Validation** - Protected fields detected
3. ✅ **Exception Thrown** - `MassAssignmentException`
4. ✅ **Logged** - WARN severity with attempted field
5. ✅ **Response** - 400 Bad Request (generic message)

### **Forensic Log Example:**
```
🚨 PRIVILEGE ESCALATION ATTEMPT - User: attacker 
tried to change role from CLIENT to ADMIN
```

---

## **4. Authentication Attacks**

### **OWASP Reference:** A07:2021 – Identification and Authentication Failures

### **Attack Scenarios:**

#### **4a. Brute-Force Attack**
```bash
# Attacker tries multiple passwords rapidly
for i in {1..100}; do
  curl -X POST /api/auth/login \
    -d '{"username":"admin","password":"password'$i'"}'
done
```

**Defense:**
```java
// LoginAttemptService tracks attempts
public void recordLoginAttempt(String username, String ip, boolean success) {
    // Count failed attempts in last 15 minutes
    long failedAttempts = countRecentFailedAttempts(username);
    
    if (failedAttempts >= MAX_ATTEMPTS) {
        lockAccount(username, 30); // Lock for 30 minutes
    }
}
```

**Result:** Account locked after 5 failed attempts in 15 minutes

#### **4b. Credential Stuffing**
```bash
# Attacker uses leaked passwords from other breaches
POST /api/auth/login
{
  "username": "john@example.com",
  "password": "LeakedPasswordFromAnotherSite123"
}
```

**Defense:**
- BCrypt strength 12 makes hash comparison slow
- Account lockout prevents rapid testing
- All attempts logged with IP and User-Agent

---

## **5. Session Hijacking**

### **OWASP Reference:** A07:2021 – Identification and Authentication Failures

### **Attack Scenario:**

```bash
# Attacker steals JWT token and tries to use it
GET /api/factures
Authorization: Bearer <stolen_token>

# User logs out, attacker still tries to use token
GET /api/clients
Authorization: Bearer <revoked_token>
```

### **How MediaTech Blocks It:**

#### **Defense 1: Token Blacklist**
```java
// EnhancedJwtAuthenticationFilter checks blacklist
String token = extractJwtFromRequest(request);

if (tokenBlacklistService.isTokenBlacklisted(token)) {
    log.warn("Attempted use of blacklisted token from IP: {}", ipAddress);
    return; // Reject request
}
```

#### **Defense 2: Short Token Lifetime**
```properties
# Access tokens expire after 15 minutes
application.security.jwt.access-token.expiration=900000
```

#### **Defense 3: Token Rotation**
```java
// Refresh tokens are rotated on each use
public RefreshTokenEntity rotateRefreshToken(String token) {
    // Revoke old token
    oldToken.setRevoked(true);
    
    // Generate new token
    return createRefreshToken(user);
}
```

**Result:** Even if token is stolen, it:
- Expires in 15 minutes
- Gets blacklisted on logout
- Can't be reused after refresh

---

## **📊 Defense Summary Table**

| Attack Type | Primary Defense | Secondary Defense | Detection | Logging |
|------------|----------------|-------------------|-----------|---------|
| **SQL Injection** | Parameterized Queries | Input Validation | Pattern Matching | CRITICAL |
| **IDOR** | Ownership Validation | @PreAuthorize | Access Check | CRITICAL |
| **Mass Assignment** | DTO Design | Field Whitelist | Validator | WARN |
| **Brute Force** | Account Lockout | Rate Limiting | Attempt Tracking | WARN |
| **Session Hijacking** | Token Blacklist | Short Expiration | Token Validation | WARN |

---

## **🔍 Forensic Analysis**

All security events are stored in the `security_events` table:

```sql
SELECT * FROM security_events 
WHERE severity = 'CRITICAL' 
ORDER BY timestamp DESC 
LIMIT 100;
```

### **Available Queries:**
- Events by type
- Events by severity
- Events by user
- Events by IP address
- Timeline analysis
- Attack patterns

---

## **🎯 Testing the Defenses**

### **Test SQL Injection:**
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' OR '\''1'\''='\''1","password":"anything"}'
```

**Expected:** 400 Bad Request + CRITICAL log entry

### **Test IDOR:**
```bash
# Login as CLIENT (gets factures for client ID 1)
# Try to access facture belonging to client ID 2
curl -X GET http://localhost:8090/api/factures/999 \
  -H "Authorization: Bearer <client_token>"
```

**Expected:** 403 Forbidden + CRITICAL log entry

### **Test Mass Assignment:**
```bash
curl -X PUT http://localhost:8090/api/users/profile \
  -H "Authorization: Bearer <token>" \
  -d '{"email":"test@test.com","role":"ADMIN"}'
```

**Expected:** 400 Bad Request + WARN log entry

---

## **📝 Compliance & Standards**

### **OWASP Top 10 2021 Coverage:**
✅ A01: Broken Access Control  
✅ A02: Cryptographic Failures  
✅ A03: Injection  
✅ A04: Insecure Design  
✅ A07: Identification and Authentication Failures  
✅ A09: Security Logging and Monitoring Failures  

### **Academic Justifications:**

**Why Multiple Layers?**
- **Defense in Depth:** No single control is perfect
- **Detection:** Even if bypass occurs, it's detected and logged
- **Forensics:** Comprehensive audit trail for incident response

**Why Log Everything?**
- **Incident Response:** Understand what happened
- **Threat Intelligence:** Identify attack patterns
- **Compliance:** Meet regulatory requirements (GDPR, PCI-DSS)

---

**Document Version:** 2.0  
**Last Updated:** 2025-12-20  
**Maintained By:** Security Engineering Team
