package com.estc.mediatech_2.exception;

import com.estc.mediatech_2.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Security Exception Handler
 * 
 * Security Features:
 * - Maps security exceptions to secure HTTP responses
 * - Prevents information disclosure
 * - Logs security incidents for forensic analysis
 * - Sanitizes error messages
 * 
 * OWASP References:
 * - A01:2021 – Broken Access Control
 * - A04:2021 – Insecure Design
 * - A09:2021 – Security Logging and Monitoring Failures
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class SecurityExceptionHandler {

    private final SecurityAuditService auditService;

    /**
     * Handle Access Denied Exceptions
     * 
     * Security: Prevents information disclosure about protected resources
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        String username = extractUsername(request);
        String resource = request.getRequestURI();
        String method = request.getMethod();

        // Critical security log
        log.warn("🚨 ACCESS DENIED - User: {}, Resource: {}, Method: {}, IP: {}",
                username, resource, method, getClientIP(request));

        // Audit trail
        auditService.logUnauthorizedAccess(
                username,
                resource,
                method,
                getClientIP(request),
                "Access Denied - Insufficient permissions");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", "You do not have permission to access this resource");
        // Don't expose internal paths or reasons

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle Bad Credentials (Login Failures)
     * 
     * Security: Generic message to prevent username enumeration
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        String attemptedUsername = request.getParameter("username");
        String ipAddress = getClientIP(request);

        // Security log - authentication failure
        log.warn("🔒 AUTHENTICATION FAILED - Username: {}, IP: {}, Reason: Bad Credentials",
                attemptedUsername, ipAddress);

        // Audit trail
        auditService.logAuthenticationFailure(
                attemptedUsername,
                ipAddress,
                "Bad Credentials",
                getUserAgent(request));

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        // Generic message - don't reveal if username exists
        response.put("message", "Invalid username or password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle Account Locked Exception
     * 
     * Security: Informs user of lockout without sensitive details
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLockedAccount(
            LockedException ex,
            HttpServletRequest request) {

        String username = request.getParameter("username");
        String ipAddress = getClientIP(request);

        // Security log
        log.warn("⚠️  LOCKED ACCOUNT ACCESS ATTEMPT - Username: {}, IP: {}",
                username, ipAddress);

        // Audit trail
        auditService.logSuspiciousActivity(
                username,
                "LOCKED_ACCOUNT_ACCESS_ATTEMPT",
                ipAddress,
                "Attempt to access locked account");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Account Locked");
        response.put("message", "This account has been locked due to security concerns. Please contact support.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle IDOR (Insecure Direct Object Reference) Attempts
     * 
     * Security: Custom exception for attempted unauthorized resource access
     */
    @ExceptionHandler(InsecureDirectObjectReferenceException.class)
    public ResponseEntity<Map<String, Object>> handleIDOR(
            InsecureDirectObjectReferenceException ex,
            HttpServletRequest request) {

        String username = extractUsername(request);
        String resource = request.getRequestURI();

        // Critical security alert
        log.error("🚨🚨 IDOR ATTACK DETECTED - User: {}, Resource: {}, AttemptedAccess: {}, IP: {}",
                username, resource, ex.getAttemptedResource(), getClientIP(request));

        // High-severity audit
        auditService.logSecurityIncident(
                username,
                "IDOR_ATTACK",
                resource,
                getClientIP(request),
                "Attempted unauthorized access to resource: " + ex.getAttemptedResource());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", "Access to this resource is not authorized");
        // Don't expose what they tried to access

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle SQL Injection Attempts
     * 
     * Security: Detects and blocks potential SQL injection payloads
     */
    @ExceptionHandler(PotentialSQLInjectionException.class)
    public ResponseEntity<Map<String, Object>> handleSQLInjection(
            PotentialSQLInjectionException ex,
            HttpServletRequest request) {

        String username = extractUsername(request);
        String suspiciousInput = ex.getSuspiciousInput();

        // Critical security alert
        log.error("🚨🚨🚨 SQL INJECTION ATTEMPT DETECTED - User: {}, Input: {}, IP: {}",
                username, suspiciousInput, getClientIP(request));

        // Critical audit
        auditService.logCriticalSecurityIncident(
                username,
                "SQL_INJECTION_ATTEMPT",
                request.getRequestURI(),
                getClientIP(request),
                "Suspicious SQL patterns detected in input: " + suspiciousInput);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Invalid Input");
        response.put("message", "The provided input contains invalid characters");
        // Don't reveal what we detected

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle Mass Assignment Attempts
     * 
     * Security: Prevents modification of protected fields
     */
    @ExceptionHandler(MassAssignmentException.class)
    public ResponseEntity<Map<String, Object>> handleMassAssignment(
            MassAssignmentException ex,
            HttpServletRequest request) {

        String username = extractUsername(request);

        // Security alert
        log.warn("⚠️  MASS ASSIGNMENT ATTEMPT - User: {}, Field: {}, IP: {}",
                username, ex.getAttemptedField(), getClientIP(request));

        // Audit
        auditService.logSuspiciousActivity(
                username,
                "MASS_ASSIGNMENT_ATTEMPT",
                getClientIP(request),
                "Attempted to modify protected field: " + ex.getAttemptedField());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Invalid Request");
        response.put("message", "Unable to process the request");
        // Don't reveal which field is protected

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Generic exception handler for unexpected errors
     * 
     * Security: Prevents information disclosure through stack traces
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log full details internally
        log.error("❌ UNEXPECTED ERROR - Path: {}, Error: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        // Never expose stack traces or technical details

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Helper methods

    private String extractUsername(HttpServletRequest request) {
        try {
            return request.getUserPrincipal() != null
                    ? request.getUserPrincipal().getName()
                    : "anonymous";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
