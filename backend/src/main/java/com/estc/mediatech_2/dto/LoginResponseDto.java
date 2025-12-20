package com.estc.mediatech_2.dto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private String username;
    private String role;
    private Long id_client;
}
