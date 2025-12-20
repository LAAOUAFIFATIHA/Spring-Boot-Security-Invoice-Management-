package com.estc.mediatech_2.exception;

import lombok.Getter;

/**
 * Exception for Insecure Direct Object Reference (IDOR) attempts
 * 
 * OWASP: A01:2021 – Broken Access Control
 * 
 * Thrown when a user attempts to access a resource they don't own
 */
@Getter
public class InsecureDirectObjectReferenceException extends RuntimeException {

    private final String attemptedResource;
    private final String username;

    public InsecureDirectObjectReferenceException(String username, String attemptedResource) {
        super("IDOR attempt: User " + username + " tried to access unauthorized resource");
        this.username = username;
        this.attemptedResource = attemptedResource;
    }
}
