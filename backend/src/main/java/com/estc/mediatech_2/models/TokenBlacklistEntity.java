package com.estc.mediatech_2.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity for blacklisting revoked JWT access tokens
 * 
 * Security Enhancement: Prevents reuse of compromised or logged-out tokens
 * OWASP Reference: A01:2021 – Broken Access Control
 */
@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token_hash", columnList = "tokenHash"),
        @Index(name = "idx_expiry", columnList = "expiryDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hash of the JWT token (not storing the full token for security)
     */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Instant blacklistedAt;

    @Column(length = 100)
    private String reason; // e.g., "LOGOUT", "SECURITY_BREACH", "PASSWORD_CHANGE"
}
