package com.estc.mediatech_2.controllers;

import com.estc.mediatech_2.dto.ProduitRequestDto;
import com.estc.mediatech_2.dto.ProduitResponseDto;
import com.estc.mediatech_2.service.ProduitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping
    public ResponseEntity<ProduitResponseDto> createProduit(@RequestBody ProduitRequestDto request) {
        return ResponseEntity.ok(produitService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<ProduitResponseDto>> getAllProduits() {
        return ResponseEntity.ok(produitService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponseDto> getProduit(@PathVariable Long id) {
        ProduitResponseDto produit = produitService.findById(id);
        return produit != null ? ResponseEntity.ok(produit) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduitResponseDto> updateProduit(@PathVariable Long id,
            @RequestBody ProduitRequestDto request) {
        ProduitResponseDto updated = produitService.update(request, id);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
