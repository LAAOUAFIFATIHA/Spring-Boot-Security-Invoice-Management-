package com.estc.mediatech_2.security.validator;

import com.estc.mediatech_2.exception.MassAssignmentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Mass Assignment Validator
 * 
 * Prevents unauthorized modification of protected fields
 * 
 * Protection Strategy:
 * - Whitelist allowed fields for each entity
 * - Blacklist sensitive fields (id, role, enabled, etc.)
 * - Validate updates before applying
 * 
 * OWASP: A04:2021 – Insecure Design
 * 
 * Academic Note:
 * Mass Assignment vulnerability occurs when an application automatically
 * binds request parameters to internal object properties without filtering.
 * 
 * Example Attack:
 * POST /api/users/update
 * { "username": "john", "role": "ADMIN" } ← Attacker tries to escalate
 * privileges
 * 
 * Our Defense:
 * - Use DTOs with only allowed fields
 * - Explicitly validate which fields can be updated
 * - Never bind directly to entities
 * - Log mass assignment attempts
 */
@Component
@Slf4j
public class MassAssignmentValidator {

    // Protected fields that should NEVER be mass-assigned
    private static final List<String> PROTECTED_USER_FIELDS = Arrays.asList(
            "id", "id_user", "role", "enabled", "accountLocked",
            "lockoutTime", "failedAttempts", "verificationCode");

    private static final List<String> PROTECTED_FACTURE_FIELDS = Arrays.asList(
            "id", "id_facture", "ref_facture", "vendeur", "date_facture");

    private static final List<String> PROTECTED_CLIENT_FIELDS = Arrays.asList(
            "id", "id_client", "user");

    /**
     * Validate that no protected user fields are being modified
     */
    public void validateUserUpdate(List<String> fieldsBeingUpdated) {
        String username = getCurrentUsername();

        for (String field : fieldsBeingUpdated) {
            if (PROTECTED_USER_FIELDS.contains(field)) {
                log.error("🚨 MASS ASSIGNMENT ATTEMPT - User: {} tried to modify protected field: {}",
                        username, field);

                throw new MassAssignmentException(username, field);
            }
        }
    }

    /**
     * Validate that a non-admin cannot modify role
     */
    public void validateRoleModification(String currentRole, String newRole) {
        if (!currentRole.equals(newRole)) {
            String username = getCurrentUsername();

            // Only admins can change roles
            if (!isAdmin()) {
                log.error("🚨 PRIVILEGE ESCALATION ATTEMPT - User: {} tried to change role from {} to {}",
                        username, currentRole, newRole);

                throw new MassAssignmentException(username, "role");
            }
        }
    }

    /**
     * Validate facture field updates
     */
    public void validateFactureUpdate(List<String> fieldsBeingUpdated) {
        String username = getCurrentUsername();

        for (String field : fieldsBeingUpdated) {
            if (PROTECTED_FACTURE_FIELDS.contains(field)) {
                log.error("🚨 MASS ASSIGNMENT ATTEMPT - User: {} tried to modify protected facture field: {}",
                        username, field);

                throw new MassAssignmentException(username, field);
            }
        }
    }

    /**
     * Validate client field updates
     */
    public void validateClientUpdate(List<String> fieldsBeingUpdated) {
        String username = getCurrentUsername();

        for (String field : fieldsBeingUpdated) {
            if (PROTECTED_CLIENT_FIELDS.contains(field)) {
                log.error("🚨 MASS ASSIGNMENT ATTEMPT - User: {} tried to modify protected client field: {}",
                        username, field);

                throw new MassAssignmentException(username, field);
            }
        }
    }

    /**
     * Validate that enabled status can only be changed by admin
     */
    public void validateEnabledModification() {
        if (!isAdmin()) {
            String username = getCurrentUsername();
            log.error("🚨 UNAUTHORIZED MODIFICATION - User: {} tried to modify enabled status", username);
            throw new MassAssignmentException(username, "enabled");
        }
    }

    /**
     * General field whitelist validation
     */
    public void validateFieldAgainstWhitelist(String field, List<String> allowedFields) {
        if (!allowedFields.contains(field)) {
            String username = getCurrentUsername();
            log.warn("⚠️  Attempted modification of non-whitelisted field - User: {}, Field: {}",
                    username, field);
            throw new MassAssignmentException(username, field);
        }
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }

    /**
     * Get current username
     */
    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
