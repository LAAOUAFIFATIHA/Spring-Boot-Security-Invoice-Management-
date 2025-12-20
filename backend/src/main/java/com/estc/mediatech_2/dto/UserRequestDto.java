package com.estc.mediatech_2.dto;

import lombok.Data;

@Data
public class UserRequestDto {
    private String username;
    private String email;
    private String password;
    private String role;
}
