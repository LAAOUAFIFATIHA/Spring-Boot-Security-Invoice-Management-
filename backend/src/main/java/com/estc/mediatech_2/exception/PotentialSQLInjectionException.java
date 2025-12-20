package com.estc.mediatech_2.exception;

import lombok.Getter;

/**
 * Exception for potential SQL Injection attempts
 * 
 * OWASP: A03:2021 – Injection
 * 
 * Thrown when suspicious SQL patterns are detected in user input
 */
@Getter
public class PotentialSQLInjectionException extends RuntimeException {

    private final String suspiciousInput;
    private final String field;

    public PotentialSQLInjectionException(String field, String suspiciousInput) {
        super("Potential SQL injection detected in field: " + field);
        this.field = field;
        this.suspiciousInput = sanitizeForLogging(suspiciousInput);
    }

    private static String sanitizeForLogging(String input) {
        if (input == null)
            return "null";
        // Truncate and sanitize for logging
        String sanitized = input.length() > 100 ? input.substring(0, 100) + "..." : input;
        // Remove potentially harmful characters for logging
        return sanitized.replaceAll("[\\r\\n\\t]", " ");
    }
}
