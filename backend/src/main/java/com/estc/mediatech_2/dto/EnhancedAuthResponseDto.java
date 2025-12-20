package com.estc.mediatech_2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for enhanced authentication with refresh token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedAuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds
    private String username;
    private String role;
    private Long id_client; // For CLIENT role
}
