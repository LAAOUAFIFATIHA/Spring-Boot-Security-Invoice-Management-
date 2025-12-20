package com.estc.mediatech_2.dto;

import lombok.Data;
import java.util.List;

@Data
public class FactureRequestDto {
    private Long id_client;
    private List<LigneFactureRequestDto> ligneFactures;
}
