package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dto.UserRequestDto;
import com.estc.mediatech_2.dto.UserResponseDto;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto getUser(Long id);

    boolean verifyUser(String token);
}
