# Security Migration Guide

## 🔄 Migrating from Legacy Security to Enhanced Security

This guide explains how to migrate from the old security implementation to the new enterprise-grade security layer.

---

## 📋 Overview of Changes

### Component Mapping

| Legacy Component | Enhanced Component | Status |
|-----------------|-------------------|---------|
| `SecurityConfig` | `EnhancedSecurityConfig` | ✅ Replace |
| `JwtUtil` | `EnhancedJwtUtil` | ✅ Replace |
| `JwtAuthenticationFilter` | `EnhancedJwtAuthenticationFilter` | ✅ Replace |
| `CustomUserDetailsService` | `EnhancedUserDetailsService` | ✅ Replace |
| `AuthController` | `EnhancedAuthController` | ✅ Replace |
| `FactureController` | `SecuredFactureController` | ✅ Replace |
| - | `RefreshTokenService` | ✨ New |
| - | `TokenBlacklistService` | ✨ New |
| - | `LoginAttemptService` | ✨ New |

---

## 🗄️ Database Migration

### Step 1: Create New Tables

Run this SQL script to create the new security tables:

```sql
-- Refresh Tokens Table
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    revoked_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id_user),
    INDEX idx_token (token),
    INDEX idx_user (user_id)
);

-- Token Blacklist Table
CREATE TABLE token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    blacklisted_at DATETIME NOT NULL,
    reason VARCHAR(100),
    INDEX idx_token_hash (token_hash),
    INDEX idx_expiry (expiry_date)
);

-- Login Attempts Table
CREATE TABLE login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time DATETIME NOT NULL,
    user_agent VARCHAR(255),
    INDEX idx_username (username),
    INDEX idx_ip_address (ip_address),
    INDEX idx_attempt_time (attempt_time)
);
```

### Step 2: Alter Users Table

Add account lockout fields to existing `users` table:

```sql
ALTER TABLE users 
ADD COLUMN account_locked BOOLEAN DEFAULT FALSE,
ADD COLUMN lockout_time DATETIME,
ADD COLUMN failed_attempts INT DEFAULT 0;
```

---

## 🔧 Configuration Migration

### application.properties

**Replace:**
```properties
# Old JWT Configuration
application.security.jwt.expiration=86400000
```

**With:**
```properties
# Enhanced JWT Configuration
application.security.jwt.access-token.expiration=900000
application.security.jwt.refresh-token.expiration=604800000

# Brute-Force Protection
application.security.max-login-attempts=5
application.security.lockout-duration-minutes=30
application.security.attempt-window-minutes=15

# Enable Scheduled Tasks
spring.task.scheduling.enabled=true
```

---

## 📦 Dependency Verification

Ensure your `pom.xml` includes:

```xml
<!-- Already present, verify versions -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🔄 Component Migration Steps

### Phase 1: Backend Setup (No Downtime Required)

1. **Add New Components** (They coexist with old ones)
   - ✅ New entities created
   - ✅ New DAOs created
   - ✅ New services created
   - ✅ Enhanced security classes created

2. **Update Application Main Class**
   ```java
   @SpringBootApplication
   @EnableScheduling  // Add this annotation
   public class Mediatech2Application {
       ...
   }
   ```

3. **Run Database Migrations**
   - Execute SQL scripts from Step 1 above
   - Hibernate will auto-create tables if `ddl-auto=update`

### Phase 2: Switch Security Configuration

1. **Rename Old Files** (Don't delete yet)
   - `SecurityConfig.java` → `SecurityConfig.java.old`
   - `JwtUtil.java` → `JwtUtil.java.old`
   - `JwtAuthenticationFilter.java` → `JwtAuthenticationFilter.java.old`
   - `CustomUserDetailsService.java` → `CustomUserDetailsService.java.old`

2. **Update Application.properties**
   - Replace JWT configuration as shown above

3. **Restart Application**
   - New security layer will activate
   - Test login/logout flow

### Phase 3: Update Controllers

1. **Update AuthController References**
   - Switch from `AuthController` to `EnhancedAuthController`
   - Frontend will receive new response format with refresh token

2. **Update FactureController**
   - Switch from `FactureController` to `SecuredFactureController`
   - @PreAuthorize annotations now enforced

3. **Update Other Controllers**
   - Add `@PreAuthorize` annotations where needed
   - Remove old authorization logic from service layer

---

## 🔐 Frontend Migration

### Login Flow Changes

**Old Response:**
```json
{
    "token": "eyJhbGc...",
    "username": "john_doe",
    "role": "CLIENT"
}
```

**New Response:**
```json
{
    "accessToken": "eyJhbGc...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "username": "john_doe",
    "role": "CLIENT",
    "id_client": 123
}
```

### Required Frontend Changes

1. **Update AuthService**
   ```typescript
   // Store both tokens
   localStorage.setItem('accessToken', response.accessToken);
   localStorage.setItem('refreshToken', response.refreshToken);
   ```

2. **Add Token Refresh Logic**
   ```typescript
   refreshToken(): Observable<any> {
       return this.http.post('/api/auth/refresh', {
           refreshToken: localStorage.getItem('refreshToken')
       });
   }
   ```

3. **Update HTTP Interceptor**
   ```typescript
   // Add refresh token logic when access token expires
   if (error.status === 401) {
       return this.authService.refreshToken().pipe(
           switchMap((tokens) => {
               localStorage.setItem('accessToken', tokens.accessToken);
               localStorage.setItem('refreshToken', tokens.refreshToken);
               return next.handle(this.addToken(req, tokens.accessToken));
           })
       );
   }
   ```

4. **Update Logout**
   ```typescript
   logout() {
       const accessToken = localStorage.getItem('accessToken');
       return this.http.post('/api/auth/logout', {}, {
           headers: { Authorization: `Bearer ${accessToken}` }
       }).pipe(
           finalize(() => {
               localStorage.removeItem('accessToken');
               localStorage.removeItem('refreshToken');
           })
       );
   }
   ```

---

## 🧪 Testing Checklist

### Authentication Tests

- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Account lockout after 5 failed attempts
- [ ] Account auto-unlock after 30 minutes
- [ ] Refresh token works correctly
- [ ] Token rotation on refresh
- [ ] Logout blacklists access token
- [ ] Blacklisted token rejected

### Authorization Tests

- [ ] ADMIN can access dashboard
- [ ] VENDEUR can create factures
- [ ] CLIENT can view only their factures
- [ ] Unauthorized access returns 403
- [ ] PDF download requires ownership
- [ ] Method-level security enforced

### Security Headers Tests

Use browser DevTools or `curl -I` to verify:

```bash
curl -I https://your-api.com/api/factures

# Should see:
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# X-Frame-Options: DENY
# X-Content-Type-Options: nosniff
# Content-Security-Policy: default-src 'self'; ...
```

---

## 🚨 Rollback Plan

If issues arise, you can quickly rollback:

1. **Restore Old Configuration**
   - Rename `.old` files back to original names
   - Remove `@EnableScheduling` from main class
   - Revert `application.properties`

2. **Database Rollback** (if needed)
   ```sql
   -- Remove new tables
   DROP TABLE IF EXISTS login_attempts;
   DROP TABLE IF EXISTS token_blacklist;
   DROP TABLE IF EXISTS refresh_tokens;
   
   -- Remove new columns
   ALTER TABLE users 
   DROP COLUMN account_locked,
   DROP COLUMN lockout_time,
   DROP COLUMN failed_attempts;
   ```

3. **Restart Application**

---

## 📊 Performance Impact

### Expected Changes

| Metric | Before | After | Impact |
|--------|--------|-------|--------|
| Login Time | ~100ms | ~150ms | +50ms (BCrypt 12) |
| Token Validation | ~5ms | ~10ms | +5ms (Blacklist check) |
| Database Size | Baseline | +5-10MB | Login history |
| Memory Usage | Baseline | +50MB | Token caches |

### Optimization Tips

1. **Cache Blacklist Checks** (Future Enhancement)
   - Use Redis for distributed caching
   - 95% reduction in DB queries

2. **Index Optimization**
   - Ensure indices on frequently queried columns
   - Regular `ANALYZE TABLE` maintenance

3. **Token Cleanup**
   - Scheduled jobs run off-peak hours
   - Configurable retention periods

---

## 🔍 Monitoring & Logging

### New Log Entries

**Successful Login:**
```
INFO - Login attempt for user: john_doe from IP: 192.168.1.100
INFO - Successful login for user: john_doe
```

**Failed Login:**
```
WARN - Failed login attempt for user: john_doe from IP: 192.168.1.100
WARN - Account locked due to 5 failed login attempts: john_doe
```

**Token Refresh:**
```
INFO - Token refreshed for user: john_doe
```

**Blacklisted Token Attempt:**
```
WARN - Attempted use of blacklisted token from IP: 192.168.1.100
```

### Metrics to Track

- Login success/failure rate
- Account lockout frequency
- Token refresh rate
- Blacklist size growth
- Average response times

---

## 📚 Support & Documentation

- **Security Implementation:** `SECURITY_IMPLEMENTATION.md`
- **OWASP Guidelines:** https://owasp.org/Top10/
- **Spring Security Docs:** https://docs.spring.io/spring-security/

---

## ✅ Migration Checklist

### Backend
- [ ] Database migrations executed
- [ ] New entities and DAOs added
- [ ] Enhanced security services deployed
- [ ] Configuration updated
- [ ] @EnableScheduling added
- [ ] Old controllers renamed/deprecated
- [ ] New controllers activated
- [ ] Application restarted successfully

### Frontend
- [ ] AuthService updated for new response
- [ ] Refresh token logic implemented
- [ ] HTTP interceptor enhanced
- [ ] Logout updated
- [ ] Error handling improved
- [ ] Token storage secure

### Testing
- [ ] All authentication flows tested
- [ ] Authorization rules verified
- [ ] Security headers confirmed
- [ ] Performance benchmarked
- [ ] Logs reviewed

### Production
- [ ] Environment variables configured
- [ ] HTTPS enforced
- [ ] Monitoring alerts set up
- [ ] Backup plan documented
- [ ] Team trained on new features

---

**Migration Version:** 1.0  
**Estimated Migration Time:** 2-3 hours  
**Complexity:** Medium  
**Risk Level:** Low (Gradual migration supported)
