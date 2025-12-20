package com.estc.mediatech_2.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id_user;
    private String username;
    private String role;
}
