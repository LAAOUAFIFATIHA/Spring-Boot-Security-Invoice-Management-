package com.estc.mediatech_2.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    // Role constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_VENDEUR = "VENDEUR";
    public static final String ROLE_CLIENT = "CLIENT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;

    private String username;
    private String email;
    private String password;
    private String role; // ADMIN, VENDEUR, CLIENT

    private boolean enabled = true;
    private String verificationCode;

    // Account lockout fields for brute-force protection
    private boolean accountLocked = false;
    private java.time.Instant lockoutTime;
    private Integer failedAttempts = 0;

}
