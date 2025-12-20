package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dto.ProduitRequestDto;
import com.estc.mediatech_2.dto.ProduitResponseDto;
import java.util.List;

public interface ProduitService {
    ProduitResponseDto save(ProduitRequestDto produitRequestDto);

    ProduitResponseDto findById(Long id);

    ProduitResponseDto update(ProduitRequestDto produitRequestDto, Long id);

    void delete(Long id);

    List<ProduitResponseDto> findAll();
}
