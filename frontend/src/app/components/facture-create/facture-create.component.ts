import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FactureService } from '../../services/facture.service';
import { ClientService } from '../../services/client.service';
import { ProduitService } from '../../services/produit.service';
import { AuthService } from '../../services/auth.service';
import { Client } from '../../models/client';
import { Produit } from '../../models/produit';
import { Facture, LigneFacture } from '../../models/facture';

@Component({
  selector: 'app-facture-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './facture-create.component.html',
  styleUrls: ['./facture-create.component.css']
})
export class FactureCreateComponent implements OnInit {
  clients: Client[] = [];
  produits: Produit[] = [];
  selectedClient: number | null = null;
  isClient = false;

  lignes: LigneFacture[] = [];
  newLine: LigneFacture = { id_produit: undefined, qte: 1 };

  createdFactureId: number | null = null;

  constructor(
    private factureService: FactureService,
    private clientService: ClientService,
    private produitService: ProduitService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    if (this.authService.hasRole('CLIENT')) {
      this.isClient = true;
      this.selectedClient = this.authService.getClientId();
    } else {
      this.clientService.getClients().subscribe(data => this.clients = data || []);
    }
    this.produitService.getProduits().subscribe(data => this.produits = data || []);
  }

  addLine() {
    if (this.newLine.id_produit && this.newLine.qte > 0) {
      // Find product to display name if needed, but for now just ID
      const product = this.produits.find(p => p.id_produit == this.newLine.id_produit);
      this.lignes.push({ ...this.newLine, produit: product }); // Store full product for display
      this.newLine = { id_produit: undefined, qte: 1 };
    }
  }

  removeLine(index: number) {
    this.lignes.splice(index, 1);
  }

  saveFacture() {
    if (this.selectedClient && this.lignes.length > 0) {
      const facture: Facture = {
        id_client: this.selectedClient,
        ligneFactures: this.lignes.map(l => ({ id_produit: l.id_produit, qte: l.qte }))
      };

      this.factureService.createFacture(facture).subscribe(res => {
        this.createdFactureId = res.id_facture!;
        this.lignes = [];
        if (!this.isClient) {
          this.selectedClient = null;
        }
        alert('Commande créée avec succès ! Elle est maintenant en attente de validation par le vendeur.');
      });
    }
  }

  // PDF download is now handled in the dashboard after validation
}
