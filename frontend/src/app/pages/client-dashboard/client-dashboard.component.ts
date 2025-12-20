import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FactureService } from '../../services/facture.service';
import { ProduitService } from '../../services/produit.service';
import { CartService, CartItem } from '../../services/cart.service';
import { Facture } from '../../models/facture';
import { Produit } from '../../models/produit';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="app-container">
      
      <!-- Premium Floating Navbar -->
      <nav class="glass-nav">
        <div class="nav-brand">
          <div class="logo-icon">M</div>
          <span class="brand-text">MediaTech</span>
        </div>

        <div class="nav-links">
          <a [class.active]="activeTab === 'shop'" (click)="activeTab = 'shop'">Catalogue</a>
          <a [class.active]="activeTab === 'orders'" (click)="activeTab = 'orders'">Commandes</a>
        </div>

        <div class="nav-actions">
           <div class="cart-trigger" (click)="toggleCart()" [class.has-items]="cartCount > 0">
             <span class="icon">🛒</span>
             <span class="badge-pill" *ngIf="cartCount > 0">{{ cartCount }}</span>
           </div>
           
           <div class="user-menu">
             <div class="avatar">{{ username?.charAt(0) }}</div>
             <button class="btn-logout-icon" (click)="logout()" title="Déconnexion">⏻</button>
           </div>
        </div>
      </nav>

      <!-- Main Content Area -->
      <main class="main-body">
        
        <!-- Shop View -->
        <div *ngIf="activeTab === 'shop'" class="animate-fade-in">
           <header class="page-header">
              <h1>Catalogue Produits</h1>
              <p>Explorez nos solutions technologiques de pointe.</p>
           </header>

           <div class="products-grid">
              <div class="product-card glass-panel" *ngFor="let p of productos">
                 <div class="stock-tag" [ngClass]="{'low': (p.qte_stock || 0) < 10, 'out': (p.qte_stock || 0) === 0}">
                    {{ (p.qte_stock || 0) > 0 ? ((p.qte_stock || 0) < 10 ? 'Dernières pièces' : 'En Stock') : 'Épuisé' }}
                 </div>
                 
                 <div class="card-content">
                    <h3>{{ p.libelle_produit }}</h3>
                    <div class="price-tag">{{ p.prix_unitaire | currency:'EUR' }}</div>
                 </div>

                 <button class="btn-primary full-btn" (click)="addToCart(p)" [disabled]="(p.qte_stock || 0) === 0">
                    {{ (p.qte_stock || 0) > 0 ? 'Ajouter' : 'Indisponible' }}
                 </button>
              </div>
           </div>
        </div>

        <!-- Orders View -->
        <div *ngIf="activeTab === 'orders'" class="animate-fade-in">
           <header class="page-header">
              <h1>Historique des Commandes</h1>
              <p>Suivez le statut de vos acquisitions récentes.</p>
           </header>

           <div class="glass-panel table-wrapper">
              <table>
                 <thead>
                    <tr>
                       <th>Référence</th>
                       <th>Date</th>
                       <th>Montant</th>
                       <th>Statut</th>
                       <th>Document</th>
                    </tr>
                 </thead>
                 <tbody>
                    <tr *ngFor="let f of factures">
                       <td class="font-mono">{{ f.ref_facture }}</td>
                       <td>{{ f.date_facture | date:'mediumDate' }}</td>
                       <td class="font-bold">{{ f.montant_total | currency:'EUR' }}</td>
                       <td>
                          <span class="badge" [ngClass]="getBadgeClass(f.status)">{{ f.status }}</span>
                       </td>
                       <td>
                          <button *ngIf="f.status === 'VALIDEE'" (click)="downloadPdf(f.id_facture!)" class="link-btn">
                             Télécharger PDF
                          </button>
                          <span *ngIf="f.status !== 'VALIDEE'" class="text-muted">–</span>
                       </td>
                    </tr>
                    <tr *ngIf="factures.length === 0">
                        <td colspan="5" class="empty-state">Aucune commande pour le moment.</td>
                    </tr>
                 </tbody>
              </table>
           </div>
        </div>

      </main>

      <!-- Glass Cart Drawer -->
      <div class="cart-backdrop" *ngIf="showCart" (click)="toggleCart()"></div>
      <div class="cart-drawer glass-panel" [class.open]="showCart">
         <div class="drawer-header">
            <h3>Mon Panier</h3>
            <button class="close-btn" (click)="toggleCart()">×</button>
         </div>
         
         <div class="drawer-body">
            <div *ngIf="cartItems.length === 0" class="empty-cart">
               <span class="icon-lg">🛒</span>
               <p>Votre panier est vide</p>
            </div>

            <div class="cart-row" *ngFor="let item of cartItems">
               <div class="item-info">
                  <h4>{{ item.produit.libelle_produit }}</h4>
                  <div class="item-meta">{{ item.quantity }} x {{ item.produit.prix_unitaire | currency:'EUR' }}</div>
               </div>
               <div class="item-right">
                  <div class="item-price">{{ (item.produit.prix_unitaire || 0) * item.quantity | currency:'EUR' }}</div>
                  <button class="btn-trash" (click)="removeFromCart(item)">Supprimer</button>
               </div>
            </div>
         </div>

         <div class="drawer-footer" *ngIf="cartItems.length > 0">
            <div class="total-line">
               <span>Total</span>
               <span class="highlight">{{ cartTotal | currency:'EUR' }}</span>
            </div>
            <button class="btn-primary" (click)="checkout()">Commander</button>
         </div>
      </div>

    </div>
  `,
  styles: [`
    :host {
        display: block;
        min-height: 100vh;
    }

    .app-container {
        max-width: 1400px;
        margin: 0 auto;
        padding: 20px;
    }

    /* Glass Navbar */
    .glass-nav {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: rgba(255, 255, 255, 0.03);
        backdrop-filter: blur(16px);
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: 16px;
        padding: 15px 30px;
        margin-bottom: 40px;
        position: sticky; top: 20px; z-index: 50;
        box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
    }

    .nav-brand {
        display: flex; align-items: center; gap: 12px;
    }
    .logo-icon {
        width: 32px; height: 32px;
        background: linear-gradient(135deg, #10b981 0%, #3b82f6 100%);
        color: white; border-radius: 8px;
        display: flex; align-items: center; justify-content: center;
        font-weight: bold; font-family: 'Plus Jakarta Sans', sans-serif;
    }
    .brand-text { font-weight: 700; font-size: 1.1rem; letter-spacing: -0.5px; }

    .nav-links {
        display: flex; gap: 30px;
    }
    .nav-links a {
        color: var(--text-muted);
        cursor: pointer;
        font-weight: 500;
        transition: 0.2s;
        padding: 8px 12px;
        border-radius: 8px;
    }
    .nav-links a:hover, .nav-links a.active {
        color: white;
        background: rgba(255,255,255,0.05);
    }

    .nav-actions { display: flex; align-items: center; gap: 20px; }

    .cart-trigger { cursor: pointer; position: relative; font-size: 1.2rem; padding: 8px; border-radius: 50%; transition: 0.2s; }
    .cart-trigger:hover { background: rgba(255,255,255,0.1); }
    .badge-pill { 
        position: absolute; top: -2px; right: -2px;
        background: var(--accent); color: white;
        font-size: 0.65rem; padding: 2px 6px; border-radius: 10px; font-weight: bold;
    }

    .user-menu { display: flex; align-items: center; gap: 12px; border-left: 1px solid rgba(255,255,255,0.1); padding-left: 20px; }
    .avatar { 
        width: 32px; height: 32px; background: rgba(255,255,255,0.1); border-radius: 50%; 
        display: flex; align-items: center; justify-content: center; font-weight: 600; 
    }
    .btn-logout-icon { background: none; border: none; font-size: 1.2rem; color: var(--text-muted); cursor: pointer; transition: 0.2s; }
    .btn-logout-icon:hover { color: #f87171; }

    /* Content */
    .page-header { margin-bottom: 40px; text-align: center; }
    .page-header h1 { font-size: 2.5rem; font-weight: 700; margin: 0 0 10px; background: linear-gradient(to right, white, #a1a1aa); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .page-header p { color: var(--text-muted); }

    .products-grid {
        display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 24px;
    }

    .product-card {
        display: flex; flex-direction: column; justify-content: space-between; height: 100%;
        position: relative; overflow: hidden;
    }
    
    .stock-tag {
        position: absolute; top: 12px; right: 12px;
        font-size: 0.7rem; font-weight: 600; padding: 4px 8px; border-radius: 20px;
        background: rgba(16, 185, 129, 0.1); color: #10b981;
    }
    .stock-tag.low { color: #fb923c; background: rgba(251, 146, 60, 0.1); }
    .stock-tag.out { color: #f87171; background: rgba(248, 113, 113, 0.1); }

    .card-content h3 { font-size: 1.1rem; margin: 20px 0 10px; font-weight: 600; }
    .price-tag { font-size: 1.5rem; font-weight: 700; color: white; margin-bottom: 20px; }

    .full-btn { width: 100%; margin-top: auto; }
    .full-btn:disabled { opacity: 0.5; cursor: not-allowed; background: #333; box-shadow: none; }

    /* Drawer */
    .cart-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); z-index: 90; }
    .cart-drawer {
        position: fixed; top: 20px; right: 20px; bottom: 20px; width: 400px;
        z-index: 100; transform: translateX(120%); transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
        display: flex; flex-direction: column;
    }
    .cart-drawer.open { transform: translateX(0); }

    .drawer-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid rgba(255,255,255,0.1); }
    .drawer-body { flex: 1; overflow-y: auto; }
    
    .cart-row { display: flex; justify-content: space-between; margin-bottom: 15px; padding: 10px; background: rgba(0,0,0,0.2); border-radius: 8px; }
    .item-info h4 { margin: 0 0 4px; font-size: 0.95rem; }
    .item-meta { font-size: 0.8rem; color: var(--text-muted); }
    .item-right { text-align: right; }
    .item-price { font-weight: bold; margin-bottom: 4px; }
    .btn-trash { background: none; border: none; font-size: 0.75rem; color: #f87171; cursor: pointer; }
    
    .drawer-footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); }
    .total-line { display: flex; justify-content: space-between; font-size: 1.2rem; font-weight: 700; margin-bottom: 20px; }

    .empty-cart { text-align: center; margin-top: 50%; transform: translateY(-50%); color: var(--text-muted); }
    .icon-lg { font-size: 3rem; display: block; margin-bottom: 10px; opacity: 0.5; }

    .link-btn { background: none; border: none; color: #3b82f6; cursor: pointer; text-decoration: underline; font-size: 0.9rem; }
    .font-mono { font-family: 'JetBrains Mono', monospace; opacity: 0.8; }
    .empty-state { text-align: center; padding: 40px; color: var(--text-muted); }
  `]
})
export class ClientDashboardComponent implements OnInit {
  username: string | null = '';
  factures: Facture[] = [];
  productsAll: Produit[] = [];
  cartItems: CartItem[] = [];
  clientId: number | null = null;
  activeTab: 'shop' | 'orders' = 'shop';
  showCart = false;

  constructor(
    private authService: AuthService,
    private factureService: FactureService,
    private produitService: ProduitService,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit() {
    this.username = this.authService.getUsername();
    this.clientId = this.authService.getClientId();
    this.loadProduits();
    this.loadFactures();

    this.cartService.items$.subscribe(items => {
      this.cartItems = items;
    });
  }

  loadProduits() {
    this.produitService.getProduits().subscribe(data => this.productsAll = data);
  }

  // Getter for template compatibility
  get productos(): Produit[] {
    return this.productsAll;
  }

  loadFactures() {
    if (!this.clientId) return;
    this.factureService.getFactures().subscribe({
      next: (data) => {
        this.factures = data.filter(f => f.client?.id_client === this.clientId);
      },
      error: (err) => console.error(err)
    });
  }

  addToCart(produit: Produit) {
    this.cartService.addToCart(produit);
    this.showCart = true;
  }

  removeFromCart(item: CartItem) {
    if (item.produit.id_produit) {
      this.cartService.removeFromCart(item.produit.id_produit);
    }
  }

  toggleCart() {
    this.showCart = !this.showCart;
  }

  get cartCount(): number {
    return this.cartItems.reduce((acc, item) => acc + item.quantity, 0);
  }

  get cartTotal(): number {
    return this.cartItems.reduce((acc, item) => acc + ((item.produit.prix_unitaire || 0) * item.quantity), 0);
  }

  checkout() {
    if (!this.clientId) return;

    const ligneFactures = this.cartItems.map(item => ({
      id_produit: item.produit.id_produit!,
      qte: item.quantity
    }));

    const request = {
      id_client: this.clientId,
      ligneFactures: ligneFactures
    };

    this.factureService.createFacture(request).subscribe({
      next: () => {
        alert('Commande validée !');
        this.cartService.clearCart();
        this.showCart = false;
        this.activeTab = 'orders';
        this.loadFactures();
      },
      error: (err) => alert('Erreur: Stock insuffisant')
    });
  }

  downloadPdf(id: number) {
    this.factureService.downloadPdf(id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `FACTURE_${id}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
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
