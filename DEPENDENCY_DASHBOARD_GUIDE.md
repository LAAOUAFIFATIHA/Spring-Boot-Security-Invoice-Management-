# 🔐 Dashboard de Sécurité et Dépendances - Guide d'Utilisation

## 📋 Vue d'Ensemble

Un nouveau dashboard a été ajouté à l'interface d'administration pour surveiller les bibliothèques utilisées et leurs vulnérabilités de sécurité.

## 🎯 Fonctionnalités Implémentées

### Backend (Spring Boot)

#### 1. **Nouveaux DTOs**
- `DependencyDTO` - Représente une bibliothèque avec ses informations
- `VulnerabilityDTO` - Représente une vulnérabilité de sécurité (CVE)
- `DependencyReportDTO` - Rapport complet avec statistiques

#### 2. **Service d'Analyse**
- `DependencyAnalysisService` - Analyse le fichier `pom.xml`
- Détecte les bibliothèques et leurs versions
- Vérifie les vulnérabilités connues
- Calcule le niveau de risque global

#### 3. **API REST**
- **Endpoint**: `GET /api/dependencies/report`
- **Accès**: Administrateurs uniquement
- **Response**: Rapport complet JSON

### Frontend (Angular)

#### 1. **Nouveau Composant**
- `DependencyDashboardComponent` - Dashboard complet et interactif

#### 2. **Fonctionnalités du Dashboard**

##### 📊 Cartes de Résumé
- **Dépendances Totales** - Nombre total de bibliothèques
- **Dépendances Vulnérables** - Nombre de bibliothèques avec vulnérabilités
- **Niveau de Risque Global** - NONE, LOW, MEDIUM, HIGH, CRITICAL
- **Dernière Analyse** - Timestamp de génération

##### 🔍 Répartition des Vulnérabilités
- Nombre de vulnérabilités **CRITICAL**
- Nombre de vulnérabilités **HIGH**
- Nombre de vulnérabilités **MEDIUM**
- Nombre de vulnérabilités **LOW**

##### 🔎 Filtres Interactifs
- **Par Catégorie**: compile, runtime, test, provided
- **Par Niveau de Risque**: CRITICAL, HIGH, MEDIUM, LOW, NONE
- **Recherche Textuelle**: Par nom d'artifact ou groupId

##### 📚 Tableau des Dépendances
Pour chaque bibliothèque:
- Nom complet (groupId:artifactId)
- Version utilisée
- Catégorie (scope)
- Niveau de risque
- Nombre de vulnérabilités
- Bouton de détails

##### 🔐 Détails des Vulnérabilités
Pour chaque CVE:
- **Identifiant CVE** (ex: CVE-2023-xxxxx)
- **Sévérité** avec code couleur
- **Score CVSS** (0-10)
- **Description** de la vulnérabilité
- **Versions affectées**
- **Version corrigée** recommandée
- **Lien de référence** vers NVD/CVE

##### 💡 Recommandations de Sécurité
- Actions recommandées basées sur les vulnérabilités trouvées
- Liste des mises à jour prioritaires
- Bonnes pratiques OWASP

##### 🛡️ Bonnes Pratiques
- Inventaire des dépendances
- Mises à jour régulières
- Surveillance continue
- Tests après modification

## 🚀 Accès au Dashboard

### Pour l'Administrateur:

1. **Connexion**
   - URL: `http://localhost:4200`
   - Username: `fatihaa`
   - Password: `fatiha1233`

2. **Navigation**
   - Une fois connecté, vous êtes sur le dashboard admin
   - Dans la barre latérale, cliquez sur **"🔐 Sécurité & Dépendances"**

3. **Utilisation**
   - Le rapport se charge automatiquement
   - Utilisez les filtres pour affiner la recherche
   - Cliquez sur "▶ Détails" pour voir les vulnérabilités d'une bibliothèque
   - Cliquez sur "🔄 Actualiser" pour recharger le rapport

## 🎨 Design

### Palette de Couleurs par Risque

- **CRITICAL**: Rouge foncé (#c53030)
- **HIGH**: Orange (#dd6b20)
- **MEDIUM**: Jaune (#d69e2e)
- **LOW**: Vert (#38a169)
- **NONE**: Bleu (#4299e1)

### Style Moderne
- Dégradés vibrants
- Effet glassmorphism
- Animations douces au survol
- Design responsive
- Interface premium

## 📊 Exemple de Bibliothèques Analysées

```
org.springframework.boot:spring-boot-starter-web:3.2.2
org.springframework.boot:spring-boot-starter-data-jpa:3.2.2
org.springframework.boot:spring-boot-starter-security:3.2.2
com.mysql:mysql-connector-j:8.2.0
com.fasterxml.jackson.core:jackson-databind:2.15.3
io.jsonwebtoken:jjwt-api:0.11.5
org.projectlombok:lombok:1.18.30
com.itextpdf:itextpdf:5.5.13.3
```

## 🔒 Sécurité OWASP

### Conformité OWASP Top 10 2021

✅ **A06:2021 – Vulnerable and Outdated Components**
- Détection automatique des composants vulnérables
- Inventaire complet des dépendances
- Système d'alerte par niveau de risque
- Recommandations de mise à jour

### Fonctionnalités de Sécurité
- Accès restreint aux administrateurs uniquement
- Authentification JWT requise
- Audit logging des accès
- Données sensibles protégées

## 🔄 Mise à Jour Automatique

Le service analyse automatiquement:
1. Le fichier `pom.xml` du projet
2. Toutes les dépendances déclarées
3. Les vulnérabilités connues dans la base de données
4. Les patterns de vulnérabilités communes

## 💡 Prochaines Améliorations Possibles

- [ ] Intégration avec l'API NVD (National Vulnerability Database)
- [ ] Intégration avec OWASP Dependency-Check
- [ ] Génération de rapports PDF
- [ ] Notifications par email pour les critiques
- [ ] Historique des scans
- [ ] Comparaison entre différentes versions
- [ ] Export en CSV/JSON
- [ ] Graphiques de tendance
- [ ] Intégration CI/CD

## 📝 Notes Techniques

### Base de Données de Vulnérabilités
Actuellement, le système utilise une base de vulnérabilités en dur (`KNOWN_VULNERABILITIES` Map).

Pour une vraie production, il faudrait:
- Intégrer l'API NVD (https://nvd.nist.gov/developers)
- Utiliser OWASP Dependency-Check Maven Plugin
- Mettre en place une base de données de vulnérabilités mise à jour

### Performance
- Le rapport est généré à la demande
- Parsing du pom.xml en temps réel
- Possibilité de mise en cache pour de meilleures performances

## 🎯 Résultat Final

Le dashboard affiche maintenant:
- ✅ Liste complète des bibliothèques
- ✅ Versions de chaque bibliothèque
- ✅ Vulnérabilités détectées avec CVE
- ✅ Niveaux de risque avec code couleur
- ✅ Recommandations de sécurité
- ✅ Interface moderne et intuitive

## 📞 Support

Pour toute question ou problème:
1. Vérifiez que le backend est démarré (port 8090)
2. Vérifiez que le frontend est démarré (port 4200)
3. Vérifiez que vous êtes connecté en tant qu'admin
4. Consultez la console navigateur pour les erreurs
5. Consultez les logs backend pour les erreurs API
