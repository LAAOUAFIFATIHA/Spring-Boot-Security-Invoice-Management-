package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.ProduitDao;
import com.estc.mediatech_2.dto.ProduitRequestDto;
import com.estc.mediatech_2.dto.ProduitResponseDto;
import com.estc.mediatech_2.models.ProduitEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitService {

    private final ProduitDao produitDao;

    @Override
    public ProduitResponseDto save(ProduitRequestDto request) {
        ProduitEntity entity = new ProduitEntity();
        entity.setRef_produit(request.getRef_produit());
        entity.setLibelle_produit(request.getLibelle_produit());
        entity.setPrix_unitaire(request.getPrix_unitaire());
        entity.setQte_stock(request.getQte_stock());
        entity.setImageUrl(request.getImageUrl());
        return mapToDto(produitDao.save(entity));
    }

    @Override
    public ProduitResponseDto findById(Long id) {
        return produitDao.findById(id).map(this::mapToDto).orElse(null);
    }

    @Override
    public ProduitResponseDto update(ProduitRequestDto request, Long id) {
        return produitDao.findById(id).map(entity -> {
            entity.setRef_produit(request.getRef_produit());
            entity.setLibelle_produit(request.getLibelle_produit());
            entity.setPrix_unitaire(request.getPrix_unitaire());
            entity.setQte_stock(request.getQte_stock());
            entity.setImageUrl(request.getImageUrl());
            return mapToDto(produitDao.save(entity));
        }).orElse(null);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        try {
            produitDao.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "CRITICAL_ERROR: Impossible de supprimer ce produit car il est référencé dans des factures existantes. Veuillez supprimer les lignes de facture associées d'abord.");
        } catch (Exception e) {
            throw new RuntimeException(
                    "SYSTEM_ERROR: Une erreur est survenue lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public List<ProduitResponseDto> findAll() {
        return produitDao.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ProduitResponseDto mapToDto(ProduitEntity entity) {
        ProduitResponseDto dto = new ProduitResponseDto();
        dto.setId_produit(entity.getId_produit());
        dto.setRef_produit(entity.getRef_produit());
        dto.setLibelle_produit(entity.getLibelle_produit());
        dto.setPrix_unitaire(entity.getPrix_unitaire());
        dto.setQte_stock(entity.getQte_stock());
        dto.setImageUrl(entity.getImageUrl());
        return dto;
    }
}
