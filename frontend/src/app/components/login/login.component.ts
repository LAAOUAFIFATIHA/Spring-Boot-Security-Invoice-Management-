import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/user';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  credentials: LoginRequest = { username: '', password: '' };
  error = '';
  mode: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.mode = params['mode'];
      if (this.mode) {
        this.autoFillCredentials(this.mode);
      }
    });
  }

  autoFillCredentials(mode: string) {
    switch (mode) {
      case 'admin':
        this.credentials = { username: 'fatihaa', password: 'fatiha1233' };
        break;
      case 'vendeur':
        this.credentials = { username: 'vendeur', password: 'vendeur123' };
        break;
      case 'client':
        this.credentials = { username: 'c1', password: '1234567' };
        break;
    }
  }

  login() {
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        // Redirection basée sur le rôle
        const role = response.role;
        if (role === 'ADMIN') {
          this.router.navigate(['/admin-dashboard']);
        } else if (role === 'VENDEUR') {
          this.router.navigate(['/vendeur-dashboard']);
        } else if (role === 'CLIENT') {
          this.router.navigate(['/client-dashboard']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        console.error(err);
        // If authentication failed, clear any stored token to avoid retrying with an invalid one
        if (err.status === 401 || err.status === 403) {
          this.authService.logout();
        }
        if (err.status === 0) {
          this.error = 'Impossible de contacter le serveur (Backend down ?)';
        } else if (err.status === 401 || err.status === 403) {
          this.error = 'Identifiants incorrects';
        } else {
          this.error = 'Erreur technique : ' + err.status;
        }
      }
    });
  }
}
