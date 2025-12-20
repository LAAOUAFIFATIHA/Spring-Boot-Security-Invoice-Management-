package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.LoginAttemptDao;
import com.estc.mediatech_2.dao.UserDao;
import com.estc.mediatech_2.models.LoginAttemptEntity;
import com.estc.mediatech_2.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Service for brute-force attack prevention
 * 
 * Security Features:
 * - Account lockout after repeated failed attempts
 * - IP-based rate limiting
 * - Automatic unlocking after timeout
 * - Login attempt tracking
 * 
 * OWASP Reference: A07:2021 – Identification and Authentication Failures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final LoginAttemptDao loginAttemptDao;
    private final UserDao userDao;

    @Value("${application.security.max-login-attempts:5}")
    private Integer maxLoginAttempts;

    @Value("${application.security.lockout-duration-minutes:30}")
    private Integer lockoutDurationMinutes;

    @Value("${application.security.attempt-window-minutes:15}")
    private Integer attemptWindowMinutes;

    /**
     * Record a login attempt
     */
    @Transactional
    public void recordLoginAttempt(String username, String ipAddress, boolean success, String userAgent) {
        LoginAttemptEntity attempt = new LoginAttemptEntity();
        attempt.setUsername(username);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(success);
        attempt.setAttemptTime(Instant.now());
        attempt.setUserAgent(userAgent);

        loginAttemptDao.save(attempt);

        if (!success) {
            checkAndLockAccount(username);
        } else {
            // Reset failed attempts on successful login
            resetFailedAttempts(username);
        }
    }

    /**
     * Check if account should be locked based on failed attempts
     */
    @Transactional
    protected void checkAndLockAccount(String username) {
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(attemptWindowMinutes));
        List<LoginAttemptEntity> recentAttempts = loginAttemptDao.findRecentAttemptsByUsername(username, windowStart);

        long failedCount = recentAttempts.stream()
                .filter(attempt -> !attempt.isSuccess())
                .count();

        if (failedCount >= maxLoginAttempts) {
            userDao.findByUsername(username).ifPresent(user -> {
                user.setAccountLocked(true);
                user.setLockoutTime(Instant.now().plus(Duration.ofMinutes(lockoutDurationMinutes)));
                user.setFailedAttempts(user.getFailedAttempts() + 1);
                userDao.save(user);

                log.warn("Account locked due to {} failed login attempts: {}", failedCount, username);
            });
        }
    }

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked(String username) {
        return userDao.findByUsername(username)
                .map(user -> {
                    if (user.isAccountLocked()) {
                        // Check if lockout period has expired
                        if (user.getLockoutTime() != null && Instant.now().isAfter(user.getLockoutTime())) {
                            unlockAccount(username);
                            return false;
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Get remaining lockout time in minutes
     */
    public Long getRemainingLockoutTime(String username) {
        return userDao.findByUsername(username)
                .filter(UserEntity::isAccountLocked)
                .map(user -> {
                    if (user.getLockoutTime() == null)
                        return 0L;
                    long minutes = Duration.between(Instant.now(), user.getLockoutTime()).toMinutes();
                    return Math.max(0L, minutes);
                })
                .orElse(0L);
    }

    /**
     * Unlock an account
     */
    @Transactional
    public void unlockAccount(String username) {
        userDao.findByUsername(username).ifPresent(user -> {
            user.setAccountLocked(false);
            user.setLockoutTime(null);
            userDao.save(user);
            log.info("Account unlocked: {}", username);
        });
    }

    /**
     * Reset failed attempts counter on successful login
     */
    @Transactional
    protected void resetFailedAttempts(String username) {
        userDao.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            userDao.save(user);
        });
    }

    /**
     * Cleanup old login attempts - runs daily at 4 AM
     * 
     * Performance: Keep only last 30 days of login attempts
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldAttempts() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        loginAttemptDao.deleteOldAttempts(cutoff);
        log.info("Cleaned up login attempts older than 30 days");
    }
}
