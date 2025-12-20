---
description: Comment exécuter l'application MediaTech
---

# Guide d'exécution de l'application MediaTech

Ce guide vous explique comment démarrer l'application MediaTech (Backend Spring Boot + Frontend Angular).

## Prérequis

Avant de commencer, assurez-vous d'avoir installé :

1. **Java 21** - Requis pour Spring Boot 3.2.2
2. **Maven** - Pour gérer les dépendances du backend
3. **Node.js et npm** - Pour le frontend Angular
4. **MySQL** - Base de données (port 3306)
5. **Git Bash ou PowerShell** - Terminal de commande

## Configuration de la base de données

1. Démarrez votre serveur MySQL
2. L'application créera automatiquement la base de données `mediatech_db` au démarrage
3. Configuration par défaut :
   - **URL**: `jdbc:mysql://localhost:3306/mediatech_db`
   - **Username**: `root`
   - **Password**: (vide)
   - **Port Backend**: `8090`

> Si vous devez modifier ces paramètres, éditez le fichier `backend/src/main/resources/application.properties`

## Étape 1 : Démarrer le Backend (Spring Boot)

### Option A : Avec Maven (Recommandé)

```bash
cd c:\STS\mediatech_app\backend
mvn clean install
mvn spring-boot:run
```

### Option B : Avec Maven Wrapper

```bash
cd c:\STS\mediatech_app\backend
.\mvnw clean install
.\mvnw spring-boot:run
```

### Option C : Avec votre IDE (Eclipse/IntelliJ)

1. Ouvrez le projet backend dans votre IDE
2. Localisez la classe principale (probablement `Mediatech2Application.java`)
3. Cliquez droit → Run As → Spring Boot App

**Le backend démarrera sur** : `http://localhost:8090`

### Vérification du Backend

Une fois démarré, vous devriez voir dans la console :
```
Started Mediatech2Application in X.XXX seconds
```

Testez l'API : `http://localhost:8090/api` (ou l'endpoint de votre choix)

## Étape 2 : Démarrer le Frontend (Angular)

### Installation des dépendances (première fois uniquement)

```bash
cd c:\STS\mediatech_app\frontend
npm install
```

### Démarrage du serveur de développement

```bash
cd c:\STS\mediatech_app\frontend
npm start
```

Ou avec Angular CLI :

```bash
ng serve
```

**Le frontend démarrera sur** : `http://localhost:4200`

### Vérification du Frontend

Ouvrez votre navigateur et accédez à : `http://localhost:4200`

## Étape 3 : Connexion à l'application

L'application utilise JWT pour l'authentification. Un utilisateur admin est créé automatiquement au démarrage :

- **Username**: `admin`
- **Password**: `admin123`

## Résolution des problèmes courants

### Problème : Port 8090 déjà utilisé

**Solution** : Changez le port dans `backend/src/main/resources/application.properties`
```properties
server.port=8091
```
N'oubliez pas de mettre à jour l'URL de l'API dans le frontend (`frontend/src/app/app.config.ts` ou dans les services).

### Problème : MySQL ne démarre pas

**Solution** : 
1. Vérifiez que MySQL est installé et démarré
2. Vérifiez les credentials dans `application.properties`
3. Créez manuellement la base de données si nécessaire :
   ```sql
   CREATE DATABASE mediatech_db;
   ```

### Problème : Erreur CORS

**Solution** : Le backend est configuré pour accepter les requêtes depuis `http://localhost:4200`. Si vous utilisez un autre port, mettez à jour la configuration CORS dans le backend.

### Problème : `mvn` command not found

**Solution** : 
1. Installez Maven : https://maven.apache.org/download.cgi
2. Ajoutez Maven au PATH système
3. Ou utilisez le Maven Wrapper : `.\mvnw` au lieu de `mvn`

### Problème : Dépendances npm manquantes

**Solution** :
```bash
cd c:\STS\mediatech_app\frontend
rm -rf node_modules package-lock.json
npm install
```

## Commandes rapides (Résumé)

### Démarrage complet en 2 terminaux

**Terminal 1 - Backend** :
```bash
cd c:\STS\mediatech_app\backend
mvn spring-boot:run
```

**Terminal 2 - Frontend** :
```bash
cd c:\STS\mediatech_app\frontend
npm start
```

## Arrêt de l'application

- **Backend** : Appuyez sur `Ctrl+C` dans le terminal du backend
- **Frontend** : Appuyez sur `Ctrl+C` dans le terminal du frontend
- **MySQL** : Laissez-le tourner ou arrêtez le service MySQL

## Fonctionnalités de l'application

L'application MediaTech permet de :
- ✅ Gérer les clients
- ✅ Gérer les produits (avec gestion du stock)
- ✅ Créer et gérer des factures
- ✅ Authentification avec JWT (Admin, Client, Vendeur)
- ✅ Génération de PDF pour les factures
- ✅ Mise à jour automatique du stock lors de la création de factures

## Support

Si vous rencontrez des problèmes :
1. Vérifiez les logs dans la console du backend et du frontend
2. Assurez-vous que tous les prérequis sont installés
3. Vérifiez que MySQL est démarré et accessible
4. Vérifiez que les ports 8090 et 4200 sont disponibles
