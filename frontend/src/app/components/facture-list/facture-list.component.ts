import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FactureService } from '../../services/facture.service';
import { AuthService } from '../../services/auth.service';
import { Facture } from '../../models/facture';

@Component({
    selector: 'app-facture-list',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './facture-list.component.html',
    styleUrls: ['./facture-list.component.css']
})
export class FactureListComponent implements OnInit {
    factures: Facture[] = [];
    isAdmin = false;
    isVendeur = false;

    constructor(
        private factureService: FactureService,
        private authService: AuthService
    ) { }

    ngOnInit() {
        this.isAdmin = this.authService.hasRole('ADMIN');
        this.isVendeur = this.authService.hasRole('VENDEUR');
        this.loadFactures();
    }

    loadFactures() {
        this.factureService.getFactures().subscribe({
            next: (data) => {
                // Sort by date desc
                this.factures = data.sort((a, b) => new Date(b.date_facture!).getTime() - new Date(a.date_facture!).getTime());
            },
            error: (err) => console.error(err)
        });
    }

    updateStatus(facture: Facture, status: string) {
        if (!facture.id_facture) return;

        if (confirm(`Voulez-vous ${status === 'VALIDEE' ? 'valider' : 'rejeter'} cette facture ?`)) {
            this.factureService.updateStatus(facture.id_facture, status).subscribe({
                next: (updated) => {
                    // Update local list
                    const index = this.factures.findIndex(f => f.id_facture === updated.id_facture);
                    if (index !== -1) {
                        this.factures[index] = updated;
                    }
                },
                error: (err) => alert("Erreur: " + (err.error?.message || err.message))
            });
        }
    }

    calculateTotal(facture: Facture): number {
        if (!facture.ligneFactures) return 0;
        return facture.ligneFactures.reduce((acc, l) => acc + ((l.produit?.prix_unitaire || 0) * l.qte), 0);
    }
}
