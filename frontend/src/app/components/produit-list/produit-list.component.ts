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
    this.produitService.getProduits().subscribe({
      next: (data) => this.produits = data,
      error: (err) => alert("Erreur lors du chargement des produits.")
    });
  }

  getStockClass(qte: number): string {
    if (qte <= 0) return 'out';
    if (qte <= 5) return 'low-stock';
    return 'in-stock';
  }

  saveProduit() {
    if (!this.newProduit.ref_produit || !this.newProduit.libelle_produit) {
      alert("Veuillez remplir au moins la référence et le libellé.");
      return;
    }

    if (this.isEditing && this.editingId) {
      this.produitService.updateProduit(this.editingId, this.newProduit).subscribe({
        next: () => {
          this.loadProduits();
          this.resetForm();
        },
        error: (err) => alert("Erreur lors de la modification.")
      });
    } else {
      this.produitService.createProduit(this.newProduit).subscribe({
        next: () => {
          this.loadProduits();
          this.resetForm();
        },
        error: (err) => alert("Erreur lors de l'ajout. La référence existe peut-être déjà.")
      });
    }
  }

  editProduit(produit: Produit) {
    this.newProduit = { ...produit };
    this.isEditing = true;
    this.editingId = produit.id_produit || null;
  }

  deleteProduit(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
      this.produitService.deleteProduit(id).subscribe({
        next: () => {
          this.loadProduits();
        },
        error: (err) => {
          console.error('Delete failed', err);
          alert('Erreur: Impossible de supprimer ce produit. Il est probablement utilisé dans une facture.');
        }
      });
    }
  }

  resetForm() {
    this.newProduit = { ref_produit: '', libelle_produit: '', prix_unitaire: 0, qte_stock: 0, imageUrl: '' };
    this.isEditing = false;
    this.editingId = null;
  }
}
