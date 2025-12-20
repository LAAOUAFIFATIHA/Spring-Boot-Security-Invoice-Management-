import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ClientListComponent } from './components/client-list/client-list.component';
import { ProduitListComponent } from './components/produit-list/produit-list.component';
import { FactureCreateComponent } from './components/facture-create/facture-create.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { VendeurDashboardComponent } from './pages/vendeur-dashboard/vendeur-dashboard.component';
import { ClientDashboardComponent } from './pages/client-dashboard/client-dashboard.component';
import { FactureListComponent } from './components/facture-list/facture-list.component';
import { DependencyDashboardComponent } from './components/dependency-dashboard/dependency-dashboard.component';
import { HomeComponent } from './pages/home/home.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },

    // Role-specific dashboards
    {
        path: 'admin-dashboard',
        component: AdminDashboardComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] }
    },
    {
        path: 'vendeur-dashboard',
        component: VendeurDashboardComponent,
        canActivate: [roleGuard],
        data: { roles: ['VENDEUR'] }
    },
    {
        path: 'client-dashboard',
        component: ClientDashboardComponent,
        canActivate: [roleGuard],
        data: { roles: ['CLIENT'] }
    },

    // Feature routes with role-based access
    {
        path: 'clients',
        component: ClientListComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'VENDEUR'] }
    },
    {
        path: 'produits',
        component: ProduitListComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'VENDEUR'] }
    },
    // Main Factures route now lists invoices
    {
        path: 'factures',
        component: FactureListComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'VENDEUR', 'CLIENT'] }
    },
    // Creation route separated
    {
        path: 'factures/create',
        component: FactureCreateComponent,
        canActivate: [roleGuard],
        data: { roles: ['VENDEUR', 'CLIENT'] } // Admin NOT included here
    },
    // Dependency Dashboard - Admin only
    {
        path: 'dependencies',
        component: DependencyDashboardComponent,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] }
    },

    // Legacy dashboard route (kept for compatibility)
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [authGuard],
        children: [
            { path: 'clients', component: ClientListComponent },
            { path: 'produits', component: ProduitListComponent },
            { path: 'factures/new', component: FactureCreateComponent },
            { path: 'dependencies', component: DependencyDashboardComponent },
            { path: '', redirectTo: 'clients', pathMatch: 'full' }
        ]
    },

    { path: 'home', component: HomeComponent },
    { path: '', component: HomeComponent, pathMatch: 'full' },
    { path: '**', redirectTo: 'login' }
];
