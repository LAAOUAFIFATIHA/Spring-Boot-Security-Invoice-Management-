import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProduitService } from '../../services/produit.service';
import { Produit } from '../../models/produit';

@Component({
  selector: 'app-produit-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './produit-list.component.html',
  styleUrls: ['./produit-list.component.css']
})
export class ProduitListComponent implements OnInit {
  produits: Produit[] = [];
  newProduit: Produit = { ref_produit: '', libelle_produit: '', prix_unitaire: 0, qte_stock: 0 };
  isEditing = false;
  editingId: number | null = null;

  constructor(private produitService: ProduitService) { }

  ngOnInit(): void {
    this.loadProduits();
  }

  loadProduits() {
    this.produitService.getProduits().subscribe(data => this.produits = data);
  }

  saveProduit() {
    if (this.isEditing && this.editingId) {
      this.produitService.updateProduit(this.editingId, this.newProduit).subscribe(() => {
        this.loadProduits();
        this.resetForm();
      });
    } else {
      this.produitService.createProduit(this.newProduit).subscribe(() => {
        this.loadProduits();
        this.resetForm();
      });
    }
  }

  editProduit(produit: Produit) {
    this.newProduit = { ...produit };
    this.isEditing = true;
    this.editingId = produit.id_produit || null;
  }

  deleteProduit(id: number) {
    if (confirm('Êtes-vous sûr ?')) {
      this.produitService.deleteProduit(id).subscribe(() => this.loadProduits());
    }
  }

  resetForm() {
    this.newProduit = { ref_produit: '', libelle_produit: '', prix_unitaire: 0, qte_stock: 0 };
    this.isEditing = false;
    this.editingId = null;
  }
}
