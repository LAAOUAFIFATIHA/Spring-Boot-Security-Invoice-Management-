package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.TokenBlacklistDao;
import com.estc.mediatech_2.models.TokenBlacklistEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Service for managing JWT Token Blacklist (revoked access tokens)
 * 
 * Security Features:
 * - Token revocation on logout
 * - Hash-based storage (doesn't store actual tokens)
 * - Automatic cleanup of expired entries
 * 
 * OWASP Reference: A01:2021 – Broken Access Control
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final TokenBlacklistDao tokenBlacklistDao;

    /**
     * Add a token to the blacklist
     * 
     * Security: Stores SHA-256 hash instead of actual token
     */
    @Transactional
    public void blacklistToken(String token, Instant expiryDate, String reason) {
        String tokenHash = hashToken(token);

        TokenBlacklistEntity blacklistEntry = new TokenBlacklistEntity();
        blacklistEntry.setTokenHash(tokenHash);
        blacklistEntry.setExpiryDate(expiryDate);
        blacklistEntry.setBlacklistedAt(Instant.now());
        blacklistEntry.setReason(reason);

        tokenBlacklistDao.save(blacklistEntry);
        log.info("Token blacklisted. Reason: {}", reason);
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        String tokenHash = hashToken(token);
        return tokenBlacklistDao.existsByTokenHash(tokenHash);
    }

    /**
     * Hash token using SHA-256
     * 
     * Security: One-way hash prevents token reconstruction from database
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    /**
     * Cleanup expired blacklist entries - runs daily at 2 AM
     * 
     * Performance: Prevents database bloat from old blacklist entries
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistDao.deleteExpiredTokens(Instant.now());
        log.info("Cleaned up expired blacklist entries");
    }
}
