package com.estc.mediatech_2.security.validator;

import com.estc.mediatech_2.exception.InsecureDirectObjectReferenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * IDOR (Insecure Direct Object Reference) Validator
 * 
 * Prevents users from accessing resources they don't own
 * 
 * Protection Strategy:
 * - Verify object ownership before access
 * - Check authorization for each resource access
 * - Log suspicious access attempts
 * 
 * OWASP: A01:2021 – Broken Access Control
 * 
 * Academic Note:
 * IDOR vulnerabilities occur when an application provides direct access to
 * objects based on user-supplied input without proper authorization checks.
 * 
 * Example Attack:
 * GET /api/factures/123 (user owns facture 456, tries to access 123)
 * 
 * Our Defense:
 * - Verify ownership before returning data
 * - Use indirect references where possible
 * - Log all unauthorized access attempts
 */
@Component
@Slf4j
public class IDORValidator {

    /**
     * Validate that the authenticated user owns the specified client
     */
    public void validateClientOwnership(Long requestedClientId, Long authenticatedClientId) {
        if (!requestedClientId.equals(authenticatedClientId)) {
            String username = getCurrentUsername();
            String attemptedResource = "client/" + requestedClientId;

            log.error("🚨 IDOR ATTEMPT DETECTED - User: {}, ClientId: {}, Attempted: {}",
                    username, authenticatedClientId, requestedClientId);

            throw new InsecureDirectObjectReferenceException(username, attemptedResource);
        }
    }

    /**
     * Validate that the authenticated user has permission to access a facture
     * 
     * Rules:
     * - ADMIN and VENDEUR can access any facture
     * - CLIENT can only access their own factures
     */
    public void validateFactureAccess(String factureOwnerUsername, String requestedBy, String userRole) {
        // Admin and Vendeur have access to all factures
        if ("ADMIN".equals(userRole) || "VENDEUR".equals(userRole)) {
            return;
        }

        // For clients, verify ownership
        if ("CLIENT".equals(userRole)) {
            if (!requestedBy.equals(factureOwnerUsername)) {
                log.error(
                        "🚨 IDOR ATTEMPT - CLIENT trying to access another client's facture: {} tried to access {}'s facture",
                        requestedBy, factureOwnerUsername);

                throw new InsecureDirectObjectReferenceException(requestedBy,
                        "facture owned by " + factureOwnerUsername);
            }
        }
    }

    /**
     * Validate generic resource ownership
     */
    public void validateOwnership(String resourceType, Long resourceId, String ownerUsername) {
        String currentUsername = getCurrentUsername();

        if (!currentUsername.equals(ownerUsername)) {
            log.error("🚨 IDOR ATTEMPT - User: {} tried to access {}/{} owned by {}",
                    currentUsername, resourceType, resourceId, ownerUsername);

            throw new InsecureDirectObjectReferenceException(currentUsername, resourceType + "/" + resourceId);
        }
    }

    /**
     * Validate that a user can only modify their own data
     */
    public void validateSelfModification(String targetUsername) {
        String currentUsername = getCurrentUsername();

        if (!currentUsername.equals(targetUsername) && !isAdmin()) {
            log.error("🚨 IDOR ATTEMPT - User: {} tried to modify user: {}",
                    currentUsername, targetUsername);

            throw new InsecureDirectObjectReferenceException(currentUsername, "user/" + targetUsername);
        }
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    /**
     * Log unauthorized access attempt (for analytics)
     */
    public void logUnauthorizedAttempt(String resourceType, Object resourceId) {
        String username = getCurrentUsername();
        log.warn("⚠️  Unauthorized access attempt - User: {}, Resource: {}/{}",
                username, resourceType, resourceId);
    }
}
