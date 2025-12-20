package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.RefreshTokenDao;
import com.estc.mediatech_2.models.RefreshTokenEntity;
import com.estc.mediatech_2.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing JWT Refresh Tokens
 * 
 * Security Features:
 * - Token rotation on refresh
 * - Automatic expiration
 * - Token revocation
 * - Cleanup of expired tokens
 * 
 * OWASP Reference: A07:2021 – Identification and Authentication Failures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenDao refreshTokenDao;

    @Value("${application.security.jwt.refresh-token.expiration:604800000}") // 7 days default
    private Long refreshTokenDurationMs;

    /**
     * Create a new refresh token for a user
     * 
     * Security: Uses UUID v4 for cryptographically secure random tokens
     */
    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setRevoked(false);

        return refreshTokenDao.save(refreshToken);
    }

    /**
     * Find and validate a refresh token
     */
    public RefreshTokenEntity verifyRefreshToken(String token) {
        RefreshTokenEntity refreshToken = refreshTokenDao.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            refreshTokenDao.delete(refreshToken);
            throw new RuntimeException("Refresh token expired or revoked. Please login again.");
        }

        return refreshToken;
    }

    /**
     * Rotate refresh token: revoke old, create new
     * 
     * Security Best Practice: Token rotation prevents token reuse attacks
     */
    @Transactional
    public RefreshTokenEntity rotateRefreshToken(String oldToken) {
        RefreshTokenEntity oldRefreshToken = verifyRefreshToken(oldToken);

        // Revoke the old token
        oldRefreshToken.setRevoked(true);
        oldRefreshToken.setRevokedAt(Instant.now());
        refreshTokenDao.save(oldRefreshToken);

        // Create new token
        return createRefreshToken(oldRefreshToken.getUser());
    }

    /**
     * Revoke all refresh tokens for a user (e.g., on password change, logout all
     * devices)
     */
    @Transactional
    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenDao.revokeAllUserTokens(user, Instant.now());
        log.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }

    /**
     * Cleanup expired and revoked tokens - runs daily at 3 AM
     * 
     * Performance: Prevents database bloat
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenDao.deleteExpiredOrRevokedTokens(Instant.now());
        log.info("Cleaned up expired and revoked refresh tokens");
    }
}
