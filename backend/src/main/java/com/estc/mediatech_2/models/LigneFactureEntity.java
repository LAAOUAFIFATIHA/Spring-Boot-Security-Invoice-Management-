package com.estc.mediatech_2.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "ligne_factures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ligne_facture;

    private Integer quantite;

    @ManyToOne(optional = false)
    @JsonBackReference
    private FactureEntity facture;

    @ManyToOne(optional = false)
    private ProduitEntity produit;

}
