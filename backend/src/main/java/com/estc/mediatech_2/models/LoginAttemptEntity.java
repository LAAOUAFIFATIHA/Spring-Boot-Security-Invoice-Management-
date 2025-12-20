package com.estc.mediatech_2.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity for tracking login attempts for brute-force protection
 * 
 * Security Enhancement: Account lockout after failed attempts
 * OWASP Reference: A07:2021 – Identification and Authentication Failures
 */
@Entity
@Table(name = "login_attempts", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_ip_address", columnList = "ipAddress")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private Instant attemptTime;

    @Column(length = 255)
    private String userAgent;
}
