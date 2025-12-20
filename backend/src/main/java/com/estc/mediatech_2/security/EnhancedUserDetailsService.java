package com.estc.mediatech_2.security;

import com.estc.mediatech_2.dao.UserDao;
import com.estc.mediatech_2.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

/**
 * Enhanced UserDetailsService with account lockout support
 * 
 * Security Enhancements:
 * - Account lockout status checking
 * - Automatic unlocking of expired lockouts
 * - Account enabled/disabled status
 * 
 * OWASP Reference: A07:2021 – Identification and Authentication Failures
 */
@Service
@RequiredArgsConstructor
public class EnhancedUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check if lockout has expired and auto-unlock
        boolean accountNonLocked = !user.isAccountLocked();
        if (user.isAccountLocked() && user.getLockoutTime() != null) {
            if (Instant.now().isAfter(user.getLockoutTime())) {
                // Lockout period has expired - unlock account
                user.setAccountLocked(false);
                user.setLockoutTime(null);
                userDao.save(user);
                accountNonLocked = true;
            }
        }

        return new User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                accountNonLocked, // accountNonLocked - UPDATED
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
    }
}
