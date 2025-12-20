package com.estc.mediatech_2.exception;

import lombok.Getter;

/**
 * Exception for Mass Assignment attempts
 * 
 * OWASP: A04:2021 – Insecure Design
 * 
 * Thrown when a user attempts to modify protected fields
 */
@Getter
public class MassAssignmentException extends RuntimeException {

    private final String attemptedField;
    private final String username;

    public MassAssignmentException(String username, String attemptedField) {
        super("Mass assignment attempt: User " + username + " tried to modify protected field: " + attemptedField);
        this.username = username;
        this.attemptedField = attemptedField;
    }
}
