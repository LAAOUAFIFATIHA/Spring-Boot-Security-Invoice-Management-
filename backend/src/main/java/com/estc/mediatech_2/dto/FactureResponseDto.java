package com.estc.mediatech_2.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FactureResponseDto {
    private Long id_facture;
    private String ref_facture;
    private LocalDateTime date_facture;
    private String status;
    private ClientResponseDto client;
    private List<LigneFactureResponseDto> ligneFactures;
}
