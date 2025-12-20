package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dto.FactureRequestDto;
import com.estc.mediatech_2.dto.FactureResponseDto;
import java.util.List;

public interface FactureService {
    FactureResponseDto createFacture(FactureRequestDto request);

    FactureResponseDto getFacture(Long id);

    List<FactureResponseDto> getAllFactures();

    List<FactureResponseDto> getFacturesByUsername(String username);

    FactureResponseDto updateStatus(Long id, String status);
}
