import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="home-wrapper">
      <div class="bg-gradient"></div>
      
      <div class="content-container">
        <header class="hero-section">
          <div class="brand-badge">MEDIATECH 2.0</div>
          <h1 class="main-title">
            Gestion Intelligente <br>
            <span class="text-gradient">Pour Votre Entreprise</span>
          </h1>
          <p class="subtitle">Une plateforme unifiée, sécurisée et moderne pour vos opérations.</p>
        </header>

        <div class="cards-grid">
          <!-- Admin -->
          <div class="access-card glass-panel" (click)="navigateToLogin('admin')">
            <div class="card-icon gradient-green">🛡️</div>
            <h3>Administration</h3>
            <p>Supervision globale et paramètres de sécurité.</p>
            <div class="arrow-link">Accéder &rarr;</div>
          </div>

          <!-- Vendeur -->
          <div class="access-card glass-panel" (click)="navigateToLogin('vendeur')">
            <div class="card-icon gradient-orange">📊</div>
            <h3>Espace Vendeur</h3>
            <p>Gestion des stocks et suivi des ventes en temps réel.</p>
            <div class="arrow-link">Accéder &rarr;</div>
          </div>

          <!-- Client -->
          <div class="access-card glass-panel" (click)="navigateToLogin('client')">
            <div class="card-icon gradient-blue">💼</div>
            <h3>Portail Client</h3>
            <p>Consultez vos commandes et documents financiers.</p>
            <div class="arrow-link">Accéder &rarr;</div>
          </div>
        </div>

        <footer class="home-footer">
          &copy; 2024 MediaTech Solutions. Tous droits réservés.
        </footer>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
    
    .home-wrapper {
        min-height: 100vh;
        position: relative;
        background: #09090b;
        color: white;
        font-family: 'Plus Jakarta Sans', sans-serif;
        overflow-x: hidden;
    }

    /* Background Animation */
    .bg-gradient {
        position: absolute; top: 0; left: 0; right: 0; height: 100%;
        background: 
            radial-gradient(circle at 15% 20%, rgba(16, 185, 129, 0.08), transparent 40%),
            radial-gradient(circle at 85% 80%, rgba(249, 115, 22, 0.08), transparent 40%);
        z-index: 0;
    }

    .content-container {
        position: relative; z-index: 2;
        max-width: 1200px; margin: 0 auto;
        padding: 80px 20px;
        display: flex; flex-direction: column; align-items: center;
    }

    /* Hero */
    .hero-section { text-align: center; margin-bottom: 80px; max-width: 800px; }
    
    .brand-badge {
        display: inline-block;
        padding: 6px 16px;
        background: rgba(255,255,255,0.05);
        border: 1px solid rgba(255,255,255,0.1);
        border-radius: 20px;
        font-size: 0.8rem; font-weight: 600; letter-spacing: 1px;
        color: #a1a1aa; margin-bottom: 20px;
        text-transform: uppercase;
    }

    .main-title {
        font-size: 4rem; font-weight: 800; line-height: 1.1; margin-bottom: 24px; letter-spacing: -1px;
    }
    
    .text-gradient {
        background: linear-gradient(to right, #10b981, #3b82f6);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }

    .subtitle { font-size: 1.25rem; color: #a1a1aa; line-height: 1.6; }

    /* Cards */
    .cards-grid {
        display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 30px; width: 100%;
    }

    .glass-panel {
        background: rgba(255, 255, 255, 0.02);
        backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.05);
        border-radius: 24px;
        padding: 40px;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
        position: relative; overflow: hidden;
    }

    .glass-panel::before {
        content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%;
        background: linear-gradient(to bottom, rgba(255,255,255,0.05), transparent);
        opacity: 0; transition: opacity 0.3s;
    }

    .glass-panel:hover {
        transform: translateY(-8px);
        border-color: rgba(255,255,255,0.1);
        box-shadow: 0 20px 40px rgba(0,0,0,0.3);
    }
    .glass-panel:hover::before { opacity: 1; }

    .card-icon {
        width: 64px; height: 64px;
        border-radius: 16px;
        display: flex; align-items: center; justify-content: center;
        font-size: 2rem; margin-bottom: 24px;
    }
    
    .gradient-green { background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(16, 185, 129, 0.05)); color: #10b981; }
    .gradient-orange { background: linear-gradient(135deg, rgba(249, 115, 22, 0.2), rgba(249, 115, 22, 0.05)); color: #f97316; }
    .gradient-blue { background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(59, 130, 246, 0.05)); color: #3b82f6; }

    .access-card h3 { font-size: 1.5rem; font-weight: 700; margin: 0 0 12px; }
    .access-card p { color: #a1a1aa; line-height: 1.6; margin-bottom: 24px; }

    .arrow-link {
        font-weight: 600; color: white; display: flex; align-items: center; opacity: 0; transform: translateX(-10px); transition: all 0.3s;
    }
    .glass-panel:hover .arrow-link { opacity: 1; transform: translateX(0); }

    .home-footer { margin-top: 100px; color: #52525b; font-size: 0.85rem; }
  `]
})
export class HomeComponent {
  constructor(private router: Router) { }

  navigateToLogin(mode: string) {
    this.router.navigate(['/login'], { queryParams: { mode: mode } });
  }
}
