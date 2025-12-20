package com.estc.mediatech_2.security.validator;

import com.estc.mediatech_2.exception.PotentialSQLInjectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * SQL Injection Validator
 * 
 * Detects and blocks potential SQL injection attempts in user input
 * 
 * Protection Strategy:
 * - Pattern matching for SQL keywords
 * - Detection of SQL meta-characters
 * - Whitelist validation
 * 
 * OWASP: A03:2021 – Injection
 * 
 * Academic Note:
 * This is a defense-in-depth measure. The primary defense is using
 * parameterized queries (PreparedStatements), which we already use.
 * This validator provides additional protection and attack detection.
 */
@Component
@Slf4j
public class SQLInjectionValidator {

    // Common SQL injection patterns
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
            Pattern.compile("('.*(--|;|/\\*|\\*/|xp_|sp_|0x).*')", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bUNION\\b.*\\bSELECT\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bSELECT\\b.*\\bFROM\\b.*\\bWHERE\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bINSERT\\b.*\\bINTO\\b.*\\bVALUES\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bUPDATE\\b.*\\bSET\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bDELETE\\b.*\\bFROM\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bDROP\\b.*\\bTABLE\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\bEXEC\\b|\\bEXECUTE\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("('\\s*OR\\s*'?1'?\\s*=\\s*'?1)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("('\\s*OR\\s*.*\\s*=\\s*.*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(CHAR\\(|CHR\\(|ASCII\\()", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(WAITFOR\\s+DELAY)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(BENCHMARK\\()", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(SLEEP\\()", Pattern.CASE_INSENSITIVE)
    };

    /**
     * Validate input for SQL injection patterns
     * 
     * @param fieldName Name of the field being validated
     * @param input     User input to validate
     * @throws PotentialSQLInjectionException if suspicious patterns detected
     */
    public void validateInput(String fieldName, String input) {
        if (input == null || input.trim().isEmpty()) {
            return; // Empty input is safe
        }

        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.error("🚨 SQL INJECTION PATTERN DETECTED - Field: {}, Pattern: {}, Input: {}",
                        fieldName, pattern.pattern(), sanitizeForLog(input));
                throw new PotentialSQLInjectionException(fieldName, input);
            }
        }
    }

    /**
     * Validate multiple inputs at once
     */
    public void validateInputs(String... inputs) {
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] != null) {
                validateInput("param_" + i, inputs[i]);
            }
        }
    }

    /**
     * Check if input contains suspicious characters
     */
    public boolean containsSuspiciousCharacters(String input) {
        if (input == null)
            return false;

        // Check for SQL comment markers
        if (input.contains("--") || input.contains("/*") || input.contains("*/")) {
            return true;
        }

        // Check for common SQL injection attempts
        return input.matches(".*[';\"\\\\].*");
    }

    /**
     * Sanitize string for safe logging (prevent log injection)
     */
    private String sanitizeForLog(String input) {
        if (input == null)
            return "null";
        // Truncate and remove newlines/tabs to prevent log injection
        String sanitized = input.length() > 100 ? input.substring(0, 100) + "..." : input;
        return sanitized.replaceAll("[\\r\\n\\t]", " ");
    }

    /**
     * Strict alphanumeric validation (for IDs, usernames, etc.)
     */
    public void validateAlphanumeric(String fieldName, String input) {
        if (input != null && !input.matches("^[a-zA-Z0-9_-]+$")) {
            log.warn("⚠️  Non-alphanumeric input detected - Field: {}, Input: {}",
                    fieldName, sanitizeForLog(input));
            throw new PotentialSQLInjectionException(fieldName, input);
        }
    }

    /**
     * Validate email format (prevent injection via email field)
     */
    public void validateEmail(String email) {
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            log.warn("⚠️  Invalid email format - Email: {}", sanitizeForLog(email));
            throw new PotentialSQLInjectionException("email", email);
        }
    }
}
