import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FactureService } from '../../services/facture.service';
import { Facture } from '../../models/facture';

@Component({
  selector: 'app-vendeur-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="app-container">
      
      <!-- Premium Floating Navbar -->
      <nav class="glass-nav">
        <div class="nav-brand">
          <div class="logo-icon orange">V</div>
          <span class="brand-text">Espace Vendeur</span>
        </div>

        <div class="nav-links">
          <a class="active">Tableau de bord</a>
          <a routerLink="/clients">Clients</a>
          <a routerLink="/produits">Produits</a>
        </div>

        <div class="nav-actions">
           <div class="user-menu">
             <div class="avatar vendor">{{ username?.charAt(0) }}</div>
             <button class="btn-logout-icon" (click)="logout()" title="Déconnexion">⏻</button>
           </div>
        </div>
      </nav>

      <!-- Main Content -->
      <main class="main-body animate-fade-in">
        
        <header class="page-header">
            <div>
                <h1>Tableau de bord</h1>
                <p>Bienvenue, {{ username }}. Voici l'état des commandes aujourd'hui.</p>
            </div>
            <div class="date-badge">{{ today | date:'fullDate' }}</div>
        </header>

        <!-- KPI Cards -->
        <div class="kpi-grid">
            <div class="kpi-card glass-panel">
                <div class="kpi-icon orange">📋</div>
                <div class="kpi-content">
                    <div class="kpi-value">{{ getPendingCount() }}</div>
                    <div class="kpi-label">À Valider</div>
                </div>
            </div>
            
            <div class="kpi-card glass-panel">
                <div class="kpi-icon green">✅</div>
                <div class="kpi-content">
                    <div class="kpi-value">{{ getValidatedCount() }}</div>
                    <div class="kpi-label">Traitées</div>
                </div>
            </div>

             <div class="kpi-card glass-panel">
                <div class="kpi-icon blue">📦</div>
                <div class="kpi-content">
                    <div class="kpi-value">--</div>
                    <div class="kpi-label">Nouveaux Produits</div>
                </div>
            </div>
        </div>

        <!-- Invoices Table -->
        <div class="glass-panel table-section">
            <div class="panel-header-row">
                <h3>Flux de Commandes</h3>
                <div class="actions">
                    <!-- Filters could go here -->
                </div>
            </div>

            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>Référence</th>
                            <th>Date</th>
                            <th>Client</th>
                            <th>Montant</th>
                            <th>Statut</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr *ngFor="let f of factures">
                            <td class="font-mono">{{ f.ref_facture }}</td>
                            <td>{{ f.date_facture | date:'mediumDate' }}</td>
                            <td class="font-bold">{{ f.client?.nom_client }} {{ f.client?.prenom_client }}</td>
                            <td>{{ f.montant_total | currency:'EUR' }}</td>
                            <td><span class="badge" [ngClass]="getBadgeClass(f.status)">{{ f.status }}</span></td>
                            <td>
                                <div class="action-buttons" *ngIf="f.status === 'EN_ATTENTE'">
                                    <button (click)="updateStatus(f, 'VALIDEE')" class="btn-icon check" title="Valider">✔</button>
                                    <button (click)="updateStatus(f, 'REFUSEE')" class="btn-icon cross" title="Refuser">✖</button>
                                </div>
                                <span *ngIf="f.status !== 'EN_ATTENTE'" class="text-muted status-text">
                                    {{ f.status === 'VALIDEE' ? 'Validé' : 'Refusé' }}
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
      </main>
    </div>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .app-container { max-width: 1400px; margin: 0 auto; padding: 20px; }

    /* Glass Navbar (Shared Style) */
    .glass-nav {
        display: flex; align-items: center; justify-content: space-between;
        background: rgba(255, 255, 255, 0.03); backdrop-filter: blur(16px);
        border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 16px;
        padding: 15px 30px; margin-bottom: 40px;
        position: sticky; top: 20px; z-index: 50; box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
    }

    .nav-brand { display: flex; align-items: center; gap: 12px; }
    .logo-icon {
        width: 32px; height: 32px; border-radius: 8px;
        display: flex; align-items: center; justify-content: center;
        font-weight: bold; font-family: 'Plus Jakarta Sans', sans-serif;
    }
    .logo-icon.orange { background: linear-gradient(135deg, #f97316 0%, #ea580c 100%); color: white; }
    .brand-text { font-weight: 700; font-size: 1.1rem; letter-spacing: -0.5px; }

    .nav-links { display: flex; gap: 30px; }
    .nav-links a { color: var(--text-muted); cursor: pointer; font-weight: 500; transition: 0.2s; padding: 8px 12px; border-radius: 8px; text-decoration: none; }
    .nav-links a:hover, .nav-links a.active { color: white; background: rgba(255,255,255,0.05); }

    .user-menu { display: flex; align-items: center; gap: 12px; }
    .avatar.vendor { width: 32px; height: 32px; background: #ea580c; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 600; color: white; }
    .btn-logout-icon { background: none; border: none; font-size: 1.2rem; color: var(--text-muted); cursor: pointer; transition: 0.2s; }
    .btn-logout-icon:hover { color: #f87171; }

    /* Page Header */
    .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 40px; }
    .page-header h1 { font-size: 2.5rem; font-weight: 700; margin: 0 0 10px; color: white; }
    .page-header p { color: var(--text-muted); margin: 0; }
    .date-badge { padding: 8px 16px; background: rgba(255,255,255,0.05); border-radius: 20px; color: #a1a1aa; font-weight: 500; font-size: 0.9rem; }

    /* KPI Grid */
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 24px; margin-bottom: 40px; }
    .kpi-card { display: flex; align-items: center; gap: 20px; padding: 24px; }
    
    .kpi-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
    .kpi-icon.orange { background: rgba(249, 115, 22, 0.1); }
    .kpi-icon.green { background: rgba(16, 185, 129, 0.1); }
    .kpi-icon.blue { background: rgba(59, 130, 246, 0.1); }
    
    .kpi-value { font-size: 2rem; font-weight: 700; color: white; line-height: 1; margin-bottom: 4px; }
    .kpi-label { color: var(--text-muted); font-size: 0.85rem; font-weight: 600; text-transform: uppercase; }

    /* Table */
    .panel-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .panel-header-row h3 { margin: 0; font-size: 1.25rem; font-weight: 600; }
    
    .btn-icon { width: 32px; height: 32px; border-radius: 8px; border: none; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; margin-right: 8px; transition: 0.2s; }
    .btn-icon.check { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    .btn-icon.check:hover { background: #10b981; color: white; }
    .btn-icon.cross { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
    .btn-icon.cross:hover { background: #ef4444; color: white; }

    .badge-success { background: rgba(16, 185, 129, 0.15); color: #34d399; }
    .badge-warning { background: rgba(249, 115, 22, 0.15); color: #fb923c; }
    .badge-danger { background: rgba(239, 68, 68, 0.15); color: #f87171; }
    
    .font-mono { font-family: 'JetBrains Mono', monospace; opacity: 0.8; }
    .font-bold { font-weight: 600; color: white; }
    .status-text { font-size: 0.9rem; }
  `]
})
export class VendeurDashboardComponent implements OnInit {
  username: string | null = '';
  factures: Facture[] = [];
  today = new Date();

  constructor(
    private authService: AuthService,
    private factureService: FactureService,
    private router: Router
  ) { }

  ngOnInit() {
    this.username = this.authService.getUsername();
    this.loadFactures();
  }

  loadFactures() {
    this.factureService.getFactures().subscribe({
      next: (data) => {
        this.factures = data.sort((a, b) => {
          if (a.status === 'EN_ATTENTE' && b.status !== 'EN_ATTENTE') return -1;
          if (a.status !== 'EN_ATTENTE' && b.status === 'EN_ATTENTE') return 1;
          return new Date(b.date_facture!).getTime() - new Date(a.date_facture!).getTime();
        });
      },
      error: (err) => console.error(err)
    });
  }

  updateStatus(facture: Facture, status: string) {
    if (!facture.id_facture) return;
    if (confirm(`Confirmer ${status}?`)) {
      this.factureService.updateStatus(facture.id_facture, status).subscribe({
        next: (updated) => {
          facture.status = updated.status;
        },
        error: (err) => alert("Erreur")
      });
    }
  }

  getPendingCount() {
    return this.factures.filter(f => f.status === 'EN_ATTENTE').length;
  }

  getValidatedCount() {
    return this.factures.filter(f => f.status === 'VALIDEE').length;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getBadgeClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'VALIDEE': return 'badge-success';
      case 'EN_ATTENTE': return 'badge-warning';
      case 'REFUSEE': return 'badge-danger';
      default: return '';
    }
  }
}
