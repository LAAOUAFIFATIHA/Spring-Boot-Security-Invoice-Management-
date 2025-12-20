package com.estc.mediatech_2.dto;

import lombok.Data;

@Data
public class ClientResponseDto {
    private Long id_client;
    private String nom_client;
    private String prenom_client;
    private String telephone;
}
