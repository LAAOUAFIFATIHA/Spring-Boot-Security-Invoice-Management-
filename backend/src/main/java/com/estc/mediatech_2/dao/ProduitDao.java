package com.estc.mediatech_2.dao;

import com.estc.mediatech_2.models.ProduitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProduitDao extends JpaRepository<ProduitEntity, Long> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM produits WHERE ref_produit = :refProduit", nativeQuery = true)
    Optional<ProduitEntity> findByRef_produit(
            @org.springframework.data.repository.query.Param("refProduit") String refProduit);
}
