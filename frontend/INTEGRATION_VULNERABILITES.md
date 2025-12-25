# Guide d'Intégration - Widget de Vulnérabilités

## 🎯 Objectif
Afficher les tests de vulnérabilités directement dans votre Admin Dashboard.

---

## ✅ Étape 1: Importer le Widget

Ouvrez `admin-dashboard.component.ts` et ajoutez l'import:

```typescript
import { VulnerabilityWidgetComponent } from '../../components/vulnerability-widget/vulnerability-widget.component';
```

Puis ajoutez-le dans les imports du component:

```typescript
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule,
    VulnerabilityWidgetComponent  // ← AJOUTEZ CETTE LIGNE
  ],
  // ...
})
```

---

## ✅ Étape 2: Ajouter le Widget dans le Template

Dans votre template HTML (dans `admin-dashboard.component.ts`), ajoutez cette section:

```html
<!-- SECTION VULNÉRABILITÉS - Ajoutez ceci où vous voulez -->
<div class="dashboard-section">
  <app-vulnerability-widget></app-vulnerability-widget>
</div>
```

**Suggestion de placement**: Juste après vos statistiques principales (stats cards).

---

## ✅ Étape 3: Démarrer MySQL et le Backend

Le widget a besoin du backend pour fonctionner:

```powershell
# 1. Démarrez MySQL (XAMPP, WAMP, ou service Windows)

# 2. Démarrez le backend
cd c:\STS\mediatech_app\backend
.\mvnw spring-boot:run

# 3. Le frontend devrait déjà tourner sur http://localhost:4200
```

---

## 📋 Exemple d'Intégration Complète

Voici un exemple de structure pour votre Admin Dashboard:

```typescript
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule,
    VulnerabilityWidgetComponent  // ← Import du widget
  ],
  template: `
    <div class="dashboard-container">
      <!-- Header existant -->
      <div class="dashboard-header">
        <!-- Votre header actuel -->
      </div>

      <div class="dashboard-content">
        
        <!-- Stats Cards existantes -->
        <div class="stats-grid">
          <!-- Vos stats actuelles -->
        </div>

        <!-- ✨ NOUVEAU: Section Vulnérabilités -->
        <div class="security-section">
          <app-vulnerability-widget></app-vulnerability-widget>
        </div>

        <!-- Navigation Cards existantes -->
        <div class="nav-grid">
          <!-- Vos cartes de navigation -->
        </div>

      </div>
    </div>
  `,
  styles: [`
    /* Vos styles existants */
    
    /* Nouveau style pour la section sécurité */
    .security-section {
      margin: 2rem 0;
      grid-column: 1 / -1; /* Prend toute la largeur */
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  // Votre code existant
}
```

---

## 🎨 Ce que le Widget Affiche

Le widget affiche automatiquement:

✅ **CVE-2017-9096** (MEDIUM) - iText PDF 5.5.13.3  
✅ **ADVISORY-2023** (LOW) - JJWT 0.11.5  
✅ **MAINTENANCE** (LOW) - Spring Boot 3.2.2  

Avec pour chaque vulnérabilité:
- 🔴 Niveau de sévérité (couleur)
- 📦 Composant affecté
- 📝 Description du problème
- ✅ **Solution de correction** (remediation)

---

## 🔧 Alternative: Page Dédiée

Si vous préférez une page complète dédiée aux vulnérabilités, vous pouvez:

1. Ajouter un bouton dans votre dashboard:
```html
<button routerLink="/security-console" class="btn-security">
  🛡️ Security Console
</button>
```

2. La route `/security-console` existe déjà et affiche:
   - User Risk Profiling
   - Vulnerability Detection
   - Financial Anomaly Detection

---

## ❓ Dépannage

### "Je ne vois rien"
1. ✅ Vérifiez que MySQL tourne (port 3306)
2. ✅ Vérifiez que le backend est démarré (port 8090)
3. ✅ Ouvrez la console du navigateur (F12) pour voir les erreurs

### "Erreur 401 Unauthorized"
- Le widget utilise le token JWT stocké dans localStorage
- Assurez-vous d'être connecté en tant qu'ADMIN

### "Backend ne démarre pas"
```powershell
# Vérifiez les logs
cd c:\STS\mediatech_app\backend
.\mvnw spring-boot:run

# Si erreur MySQL, vérifiez application.properties:
# spring.datasource.url=jdbc:mysql://localhost:3306/mediatech_db_v2
```

---

## 📸 Aperçu Visuel

Le widget aura ce style:

```
┌─────────────────────────────────────────┐
│ 🦠 Vulnerability Scanner    [3 Issues]  │
├─────────────────────────────────────────┤
│ CVE-2017-9096              [MEDIUM]     │
│ com.itextpdf:itextpdf:5.5.13.3         │
│ iText PDF library vulnerability...      │
│ ✅ Fix: Upgrade to iText 7.x           │
├─────────────────────────────────────────┤
│ ADVISORY-2023              [LOW]        │
│ io.jsonwebtoken:jjwt-api:0.11.5        │
│ Older version of JJWT...               │
│ ✅ Fix: Upgrade to 0.12.5+             │
└─────────────────────────────────────────┘
```

---

## 🚀 Prochaines Étapes

Une fois le widget intégré, vous pourrez:
1. Voir les vulnérabilités en temps réel
2. Suivre les recommandations de correction
3. Monitorer l'état de sécurité de votre application

**Besoin d'aide?** Dites-moi où vous voulez placer le widget dans votre dashboard!
