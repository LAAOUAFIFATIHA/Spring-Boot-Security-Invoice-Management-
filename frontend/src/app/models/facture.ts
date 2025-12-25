import { Client } from "./client";
import { Produit } from "./produit";

export interface LigneFacture {
    id_ligne_facture?: number;
    produit?: Produit; // Response
    id_produit?: number; // Request
    qte: number;
}

export interface Facture {
    id_facture?: number;
    ref_facture?: string;
    date_facture?: string;
    status?: string;
    client?: Client; // Response
    id_client?: number; // Request
    vendeur?: { username: string }; // Response
    montant_total?: number;
    ligneFactures: LigneFacture[];
}
