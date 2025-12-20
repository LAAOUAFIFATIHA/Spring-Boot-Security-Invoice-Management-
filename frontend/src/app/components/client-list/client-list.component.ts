import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientService } from '../../services/client.service';
import { Client } from '../../models/client';

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {
  clients: Client[] = [];
  newClient: Client = { nom_client: '', prenom_client: '', telephone: '' };
  isEditing = false;
  editingId: number | null = null;
  searchTel = '';

  constructor(private clientService: ClientService) { }

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients() {
    this.clientService.getClients().subscribe(data => this.clients = data || []); // Ensure array
  }

  search() {
    if (this.searchTel) {
      this.clientService.getClientByTel(this.searchTel).subscribe(
        client => this.clients = [client],
        err => this.clients = []
      );
    } else {
      this.loadClients();
    }
  }

  saveClient() {
    if (this.isEditing && this.editingId) {
      this.clientService.updateClient(this.editingId, this.newClient).subscribe(() => {
        this.loadClients();
        this.resetForm();
      });
    } else {
      this.clientService.createClient(this.newClient).subscribe(() => {
        this.loadClients();
        this.resetForm();
      });
    }
  }

  editClient(client: Client) {
    this.newClient = { ...client };
    this.isEditing = true;
    this.editingId = client.id_client || null;
  }

  deleteClient(id: number) {
    if (confirm('Êtes-vous sûr ?')) {
      this.clientService.deleteClient(id).subscribe(() => this.loadClients());
    }
  }

  resetForm() {
    this.newClient = { nom_client: '', prenom_client: '', telephone: '' };
    this.isEditing = false;
    this.editingId = null;
  }
}
