import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
        router.navigate(['/login']);
        return false;
    }

    const requiredRoles = route.data['roles'] as string[];

    if (requiredRoles && !authService.hasAnyRole(requiredRoles)) {
        // Rediriger vers le dashboard approprié selon le rôle
        const userRole = authService.getRole();
        if (userRole === 'ADMIN') {
            router.navigate(['/admin-dashboard']);
        } else if (userRole === 'VENDEUR') {
            router.navigate(['/vendeur-dashboard']);
        } else if (userRole === 'CLIENT') {
            router.navigate(['/client-dashboard']);
        } else {
            router.navigate(['/login']);
        }
        return false;
    }

    return true;
};
