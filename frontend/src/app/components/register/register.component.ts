import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest } from '../../models/user';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {
    registerData: RegisterRequest = {
        username: '',
        password: ''
    };
    confirmPassword = '';
    selectedRole = 'client';
    error = '';
    success = '';

    constructor(private authService: AuthService, private router: Router) { }

    register() {
        this.error = '';
        this.success = '';

        // Validation
        if (!this.registerData.username || !this.registerData.password) {
            this.error = 'Veuillez remplir tous les champs';
            return;
        }

        if (this.registerData.password !== this.confirmPassword) {
            this.error = 'Les mots de passe ne correspondent pas';
            return;
        }

        if (this.registerData.password.length < 6) {
            this.error = 'Le mot de passe doit contenir au moins 6 caractères';
            return;
        }

        // Register
        this.authService.register(this.registerData, this.selectedRole).subscribe({
            next: () => {
                this.success = 'Inscription réussie ! Redirection vers la page de connexion...';
                setTimeout(() => {
                    this.router.navigate(['/login']);
                }, 2000);
            },
            error: (err) => {
                console.error('Registration error:', err);
                if (err.error && err.error.message) {
                    this.error = err.error.message;
                } else if (err.message) {
                    this.error = 'Erreur technique: ' + err.message;
                } else {
                    this.error = 'Erreur inconnue lors de l\'inscription (' + JSON.stringify(err) + ')';
                }
            }
        });
    }
}
