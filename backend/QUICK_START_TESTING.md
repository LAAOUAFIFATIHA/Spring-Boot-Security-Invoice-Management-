# Quick Start Guide - Testing Enhanced Security Features

## 🚀 Quick Testing Guide

This guide helps you quickly test all new security features without reading the full documentation.

---

## 📋 Prerequisites

1. ✅ MySQL running on localhost:3306
2. ✅ Database `mediatech_db_v2` exists
3. ✅ Java 21 installed
4. ✅ Maven configured

---

## 🗄️ Step 1: Database Setup (2 minutes)

Run this SQL to create new tables:

```sql
-- Connect to database
USE mediatech_db_v2;

-- Add columns to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS account_locked BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS lockout_time DATETIME,
ADD COLUMN IF NOT EXISTS failed_attempts INT DEFAULT 0;

-- Create refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    revoked_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id_user)
);

-- Create token blacklist table
CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    blacklisted_at DATETIME NOT NULL,
    reason VARCHAR(100),
    INDEX idx_token_hash (token_hash)
);

-- Create login attempts table
CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time DATETIME NOT NULL,
    user_agent VARCHAR(255),
    INDEX idx_username (username)
);
```

---

## 🏃 Step 2: Start the Backend (1 minute)

```bash
cd backend
mvn spring-boot:run
```

Wait for: `Started Mediatech2Application in X.XX seconds`

---

## 🧪 Step 3: Test Enhanced Security Features

### Test 1: Register a New User (✅ Basic Functionality)

```bash
curl -X POST http://localhost:8090/api/auth/register/client \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecurePass123!",
    "role": "CLIENT"
  }'
```

**Expected Response:**
```json
{
  "id_user": 1,
  "username": "testuser",
  "role": "CLIENT"
}
```

---

### Test 2: Login (✅ Enhanced with Refresh Token)

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "SecurePass123!"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "username": "testuser",
  "role": "CLIENT",
  "id_client": 1
}
```

**✅ Key Changes:**
- Now returns BOTH access and refresh tokens
- `expiresIn` shows token lifetime (900 seconds = 15 minutes)

**Save these for next tests:**
```bash
# Save access token
ACCESS_TOKEN="<paste_accessToken_here>"

# Save refresh token
REFRESH_TOKEN="<paste_refreshToken_here>"
```

---

### Test 3: Account Lockout (🔒 Brute-Force Protection)

**Try 5 failed login attempts:**

```bash
# Attempt 1
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "wrongpass1"}'

# Attempt 2
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "wrongpass2"}'

# Attempt 3
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "wrongpass3"}'

# Attempt 4
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "wrongpass4"}'

# Attempt 5 - THIS WILL LOCK THE ACCOUNT
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "wrongpass5"}'
```

**Expected Response after 5th attempt:**
```json
{
  "error": "Account is locked due to too many failed login attempts. Try again in 30 minutes.",
  "remainingMinutes": 30
}
```

**Now try with CORRECT password:**
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "SecurePass123!"}'
```

**Expected:** Still locked! ❌ (Account is locked for 30 minutes)

**✅ Verification:**
Check the database:
```sql
SELECT username, account_locked, lockout_time, failed_attempts 
FROM users WHERE username = 'testuser';
```

**To unlock immediately for testing:**
```sql
UPDATE users 
SET account_locked = FALSE, lockout_time = NULL, failed_attempts = 0 
WHERE username = 'testuser';
```

---

### Test 4: Token Refresh (🔄 Token Rotation)

**Use the refresh token to get a new access token:**

```bash
curl -X POST http://localhost:8090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'"$REFRESH_TOKEN"'"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",  
  "refreshToken": "660e8400-e29b-41d4-a716-446655440001",  
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**✅ Key Feature: Token Rotation**
- Old refresh token is NOW REVOKED
- New refresh token issued
- Old token CANNOT be reused

**Try using old refresh token again:**
```bash
curl -X POST http://localhost:8090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'"$REFRESH_TOKEN"'"
  }'
```

**Expected:** Error! ❌ (Token is revoked)

---

### Test 5: Logout & Token Blacklisting (🚫 Token Revocation)

**Logout (blacklists access token):**

```bash
curl -X POST http://localhost:8090/api/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Response:**
```json
{
  "message": "Logged out successfully"
}
```

**Now try to use the blacklisted token:**

```bash
curl -X GET http://localhost:8090/api/factures \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected:** 401 Unauthorized ❌ (Token is blacklisted)

**✅ Verification:**
Check the database:
```sql
SELECT * FROM token_blacklist ORDER BY blacklisted_at DESC LIMIT 1;
```

You should see your token hash with reason "LOGOUT"

---

### Test 6: Secure PDF Download (🔐 Authorization)

**First, create a test user and login:**

```bash
# Register
curl -X POST http://localhost:8090/api/auth/register/client \
  -H "Content-Type: application/json" \
  -d '{
    "username": "client1",
    "password": "Pass123!",
    "email": "client1@test.com"
  }'

# Login and get token
RESPONSE=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "client1", "password": "Pass123!"}')

TOKEN=$(echo $RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
```

**Try to download a PDF:**

```bash
# Assuming facture ID 1 exists
curl -X GET "http://localhost:8090/api/factures/1/pdf" \
  -H "Authorization: Bearer $TOKEN"
```

**Scenarios:**

1. **If facture doesn't exist:** 404 Not Found ❌
2. **If facture belongs to another client:** 403 Forbidden ❌
3. **If facture status is not VALIDEE:** 403 Forbidden ❌
4. **If everything is OK:** PDF file downloads ✅

---

### Test 7: Method-Level Security (⚡ @PreAuthorize)

**Try accessing admin-only endpoints as CLIENT:**

```bash
# This should FAIL (requires ADMIN role)
curl -X GET http://localhost:8090/api/dashboard/stats \
  -H "Authorization: Bearer $TOKEN"
```

**Expected:** 403 Forbidden ❌

**Now register an ADMIN and try:**

```bash
# Register admin (will be disabled, need verification)
curl -X POST http://localhost:8090/api/auth/register/admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin1",
    "password": "AdminPass123!",
    "email": "admin@test.com"
  }'

# Manually enable in database (skip email verification for testing)
# SQL: UPDATE users SET enabled = TRUE WHERE username = 'admin1';

# Login as admin
ADMIN_RESPONSE=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin1", "password": "AdminPass123!"}')

ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# Now try admin endpoint
curl -X GET http://localhost:8090/api/dashboard/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Expected:** Success! ✅ (Returns dashboard stats)

---

### Test 8: Security Headers (🛡️ HTTP Security)

**Check security headers:**

```bash
curl -I http://localhost:8090/api/auth/login
```

**Expected Headers:**
```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Security-Policy: default-src 'self'; script-src 'self'; ...
```

✅ All headers present!

---

### Test 9: Password Strength (💪 BCrypt 12)

**Check password hashing in database:**

```sql
SELECT username, password FROM users WHERE username = 'testuser';
```

**You should see:**
```
testuser | $2a$12$AbC123...
         └──── BCrypt with strength 12
```

The `$2a$12$` prefix confirms BCrypt with 12 rounds (2^12 = 4096 iterations)

---

## 📊 Verify Database State

**Check all security tables:**

```sql
-- Login attempts
SELECT * FROM login_attempts ORDER BY attempt_time DESC LIMIT 10;

-- Refresh tokens
SELECT id, user_id, LEFT(token, 20) as token_preview, expiry_date, revoked 
FROM refresh_tokens ORDER BY created_at DESC LIMIT 5;

-- Token blacklist
SELECT id, LEFT(token_hash, 16) as hash_preview, reason, blacklisted_at 
FROM token_blacklist ORDER BY blacklisted_at DESC LIMIT 5;

-- User lockout status
SELECT username, account_locked, lockout_time, failed_attempts 
FROM users;
```

---

## 🕒 Test Scheduled Jobs

**Scheduled cleanup jobs run at:**
- 02:00 AM - Token Blacklist Cleanup
- 03:00 AM - Refresh Token Cleanup  
- 04:00 AM - Login Attempt Cleanup

**To test immediately, create expired entries:**

```sql
-- Create expired refresh token
INSERT INTO refresh_tokens 
(user_id, token, expiry_date, revoked, created_at) 
VALUES 
(1, 'test-expired-token', DATE_SUB(NOW(), INTERVAL 1 DAY), FALSE, NOW());

-- Create expired blacklist entry
INSERT INTO token_blacklist 
(token_hash, expiry_date, blacklisted_at, reason) 
VALUES 
('test-hash', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 'TEST');
```

**Wait for scheduled jobs or restart app to trigger manual cleanup**

**Verify cleanup:**
```sql
SELECT * FROM refresh_tokens WHERE token = 'test-expired-token';
-- Should be empty

SELECT * FROM token_blacklist WHERE token_hash = 'test-hash';
-- Should be empty
```

---

## 🎯 Feature Comparison

| Feature | Old Implementation | New Implementation |
|---------|-------------------|-------------------|
| **Access Token Expiration** | 24 hours | 15 minutes ✅ |
| **Refresh Tokens** | ❌ Not supported | ✅ 7-day rotation |
| **Token Revocation** | ❌ Not possible | ✅ Blacklist system |
| **Account Lockout** | ❌ No protection | ✅ After 5 attempts |
| **Password Strength** | BCrypt default (10) | ✅ BCrypt 12 |
| **Method Security** | URL-based only | ✅ @PreAuthorize |
| **Security Headers** | ❌ None | ✅ HSTS, CSP, etc. |
| **Login Tracking** | ❌ Not tracked | ✅ Full audit trail |

---

## 📝 Quick Reference Commands

### Get New Tokens
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "USER", "password": "PASS"}'
```

### Refresh Access Token
```bash
curl -X POST http://localhost:8090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "TOKEN"}'
```

### Logout (Revoke Tokens)
```bash
curl -X POST http://localhost:8090/api/auth/logout \
  -H "Authorization: Bearer TOKEN"
```

### Unlock Account (SQL)
```sql
UPDATE users 
SET account_locked = FALSE, lockout_time = NULL, failed_attempts = 0 
WHERE username = 'USERNAME';
```

---

## ✅ Success Checklist

After running all tests, you should have verified:

- [x] User registration works
- [x] Login returns access + refresh tokens
- [x] Account locks after 5 failed attempts
- [x] Locked account cannot login (even with correct password)
- [x] Token refresh works and rotates tokens
- [x] Old refresh tokens are revoked
- [x] Logout blacklists access tokens
- [x] Blacklisted tokens are rejected
- [x] Security headers are present
- [x] Method-level security enforced
- [x] BCrypt 12 used for passwords
- [x] Database tables created successfully

---

## 🚨 Troubleshooting

### Issue: Tables not created
**Solution:** Check Hibernate auto-ddl:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Issue: Token expiration too fast
**Solution:** Check application.properties:
```properties
application.security.jwt.access-token.expiration=900000  # 15 min
```

### Issue: Account stays locked
**Solution:** Manually unlock via SQL (see above)

### Issue: Scheduled jobs not running
**Solution:** Verify @EnableScheduling is present in main class

---

## 📚 Next Steps

1. ✅ All tests passed? Proceed to frontend integration
2. 📖 Read full documentation: `SECURITY_IMPLEMENTATION.md`
3. 🔄 Follow migration guide: `MIGRATION_GUIDE.md`
4. 🎨 Check visual overview: `SECURITY_VISUAL_OVERVIEW.md`

---

**Quick Start Version:** 1.0  
**Estimated Testing Time:** 15-20 minutes  
**Difficulty:** Beginner-friendly  
**Prerequisites:** Basic curl knowledge
