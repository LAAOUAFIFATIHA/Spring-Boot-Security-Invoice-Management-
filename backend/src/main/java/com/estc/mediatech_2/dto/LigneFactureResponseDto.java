package com.estc.mediatech_2.dto;

import lombok.Data;

@Data
public class LigneFactureResponseDto {
    private Long id_ligne_facture;
    private ProduitResponseDto produit;
    private Integer qte;
}
