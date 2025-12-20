package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.dao.ClientDao;
import com.estc.mediatech_2.dao.UserDao;
import com.estc.mediatech_2.dto.*;
import com.estc.mediatech_2.models.RefreshTokenEntity;
import com.estc.mediatech_2.models.UserEntity;
import com.estc.mediatech_2.security.EnhancedJwtUtil;
import com.estc.mediatech_2.security.EnhancedUserDetailsService;
import com.estc.mediatech_2.service.LoginAttemptService;
import com.estc.mediatech_2.service.RefreshTokenService;
import com.estc.mediatech_2.service.TokenBlacklistService;
import com.estc.mediatech_2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Authentication Controller with Enterprise Security Features
 * 
 * Security Features:
 * - Brute-force protection with account lockout
 * - Refresh token rotation
 * - Token blacklist on logout
 * - Login attempt tracking
 * - Detailed error responses
 * 
 * OWASP References:
 * - A01:2021 – Broken Access Control
 * - A07:2021 – Identification and Authentication Failures
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class EnhancedAuthController {

    private final AuthenticationManager authenticationManager;
    private final EnhancedUserDetailsService userDetailsService;
    private final EnhancedJwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final UserService userService;
    private final UserDao userDao;
    private final ClientDao clientDao;

    /**
     * Enhanced Login Endpoint with Brute-Force Protection
     * 
     * Security Features:
     * - Account lockout check before authentication
     * - Login attempt tracking
     * - Refresh token generation
     * - IP address logging
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("Login attempt for user: {} from IP: {}", username, ipAddress);

        try {
            // Check if account is locked
            if (loginAttemptService.isAccountLocked(username)) {
                Long remainingMinutes = loginAttemptService.getRemainingLockoutTime(username);
                String message = String.format(
                        "Account is locked due to too many failed login attempts. Try again in %d minutes.",
                        remainingMinutes);

                // Record failed attempt
                loginAttemptService.recordLoginAttempt(username, ipAddress, false, userAgent);

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", message, "remainingMinutes", remainingMinutes));
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UserEntity user = userDao.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if account is enabled
            if (!user.isEnabled()) {
                loginAttemptService.recordLoginAttempt(username, ipAddress, false, userAgent);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account not verified. Please check your email."));
            }

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);

            // Record successful login
            loginAttemptService.recordLoginAttempt(username, ipAddress, true, userAgent);

            // Build response
            EnhancedAuthResponseDto response = new EnhancedAuthResponseDto();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken.getToken());
            response.setExpiresIn(jwtUtil.getAccessTokenExpiration() / 1000); // Convert to seconds
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());

            // Add client ID for CLIENT role
            if (UserEntity.ROLE_CLIENT.equals(user.getRole())) {
                clientDao.findByUser_Username(user.getUsername())
                        .ifPresent(client -> response.setId_client(client.getId_client()));
            }

            log.info("Successful login for user: {}", username);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            loginAttemptService.recordLoginAttempt(username, ipAddress, false, userAgent);

            log.warn("Failed login attempt for user: {} from IP: {}", username, ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));

        } catch (LockedException e) {
            loginAttemptService.recordLoginAttempt(username, ipAddress, false, userAgent);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account is locked"));

        } catch (Exception e) {
            log.error("Login error for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred during login"));
        }
    }

    /**
     * Refresh Token Endpoint with Token Rotation
     * 
     * Security: Implements refresh token rotation to prevent token reuse
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        try {
            String requestRefreshToken = request.getRefreshToken();

            // Verify and rotate refresh token
            RefreshTokenEntity newRefreshToken = refreshTokenService.rotateRefreshToken(requestRefreshToken);

            // Load user and generate new access token
            UserDetails userDetails = userDetailsService.loadUserByUsername(newRefreshToken.getUser().getUsername());
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken.getToken());
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtUtil.getAccessTokenExpiration() / 1000);

            log.info("Token refreshed for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Refresh token error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    /**
     * Logout Endpoint with Token Blacklisting
     * 
     * Security: Revokes both access and refresh tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);

                // Extract user from token
                String username = jwtUtil.extractUsername(accessToken);

                // Blacklist the access token
                Instant expiration = jwtUtil.extractExpiration(accessToken).toInstant();
                tokenBlacklistService.blacklistToken(accessToken, expiration, "LOGOUT");

                // Revoke all refresh tokens for the user
                UserEntity user = userDao.findByUsername(username).orElse(null);
                if (user != null) {
                    refreshTokenService.revokeAllUserTokens(user);
                }

                log.info("User logged out: {}", username);
                return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
            }

            // If no token provided, just consider logged out
            return ResponseEntity.ok(Map.of("message", "Logged out (no active session)"));

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("message", "Logged out"));
        }
    }

    /**
     * Register endpoints (delegated to UserService)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/register/client")
    public ResponseEntity<UserResponseDto> registerClient(@RequestBody UserRequestDto request) {
        request.setRole(UserEntity.ROLE_CLIENT);
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/register/vendeur")
    public ResponseEntity<UserResponseDto> registerVendeur(@RequestBody UserRequestDto request) {
        request.setRole(UserEntity.ROLE_VENDEUR);
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<UserResponseDto> registerAdmin(@RequestBody UserRequestDto request) {
        request.setRole(UserEntity.ROLE_ADMIN);
        return ResponseEntity.ok(userService.createUser(request));
    }

    /**
     * Account verification endpoint
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("code") String code) {
        boolean isVerified = userService.verifyUser(code);
        if (isVerified) {
            return ResponseEntity.ok("Account verified successfully! You can now login.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification link.");
        }
    }

    /**
     * Get client IP address (handles proxies)
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
