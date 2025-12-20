package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dto.ClientRequestDto;
import com.estc.mediatech_2.dto.ClientResponseDto;
import java.util.List;

public interface ClientService {
    ClientResponseDto save(ClientRequestDto clientRequestDto);

    ClientResponseDto findById(Long id);

    ClientResponseDto findByTel(String tel);

    ClientResponseDto update(ClientRequestDto clientRequestDto, Long id);

    void delete(Long id);

    List<ClientResponseDto> findAll();
}
