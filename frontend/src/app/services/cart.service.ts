import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Produit } from '../models/produit';

export interface CartItem {
    produit: Produit;
    quantity: number;
}

@Injectable({
    providedIn: 'root'
})
export class CartService {
    private items = new BehaviorSubject<CartItem[]>([]);
    items$ = this.items.asObservable();

    addToCart(produit: Produit) {
        const currentItems = this.items.value;
        const existingItem = currentItems.find(i => i.produit.id_produit === produit.id_produit);

        if (existingItem) {
            existingItem.quantity++;
            this.items.next([...currentItems]);
        } else {
            this.items.next([...currentItems, { produit, quantity: 1 }]);
        }
    }

    removeFromCart(produitId: number) {
        const currentItems = this.items.value;
        this.items.next(currentItems.filter(i => i.produit.id_produit !== produitId));
    }

    clearCart() {
        this.items.next([]);
    }

    getItems(): CartItem[] {
        return this.items.value;
    }
}
