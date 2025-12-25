package com.estc.mediatech_2.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProduitRequestDto {
    private String ref_produit;
    private String libelle_produit;
    private BigDecimal prix_unitaire;
    private Integer qte_stock;
    private String imageUrl;
}
