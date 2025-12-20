package com.estc.mediatech_2.security;

import com.estc.mediatech_2.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Enhanced JWT Authentication Filter with Token Blacklist Validation
 * 
 * Security Enhancements:
 * - Blacklist validation before authentication
 * - Enhanced error handling
 * - Request path exclusions
 * - Detailed logging for security audits
 * 
 * OWASP Reference: A01:2021 – Broken Access Control
 * OWASP Reference: A07:2021 – Identification and Authentication Failures
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnhancedJwtAuthenticationFilter extends OncePerRequestFilter {

    private final EnhancedJwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip JWT processing for public endpoints
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            // Security Enhancement: Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                log.warn("Attempted use of blacklisted token from IP: {}", getClientIP(request));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been revoked");
                return;
            }

            final String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    // Check if user account is locked or disabled
                    if (!userDetails.isAccountNonLocked()) {
                        log.warn("Login attempt with locked account: {}", username);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Account is locked");
                        return;
                    }

                    if (!userDetails.isEnabled()) {
                        log.warn("Login attempt with disabled account: {}", username);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Account is disabled");
                        return;
                    }

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Successfully authenticated user: {}", username);
                } else {
                    log.warn("Invalid token for user: {}", username);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired");
            return;
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
                path.equals("/error") ||
                path.startsWith("/actuator/health");
    }

    /**
     * Get client IP address (handles proxy forwarding)
     * 
     * Security: Useful for rate limiting and audit logging
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
