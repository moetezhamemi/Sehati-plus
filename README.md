# Sehati+ 🏥

**Sehati+** est une plateforme de santé digitale full-stack permettant aux patients de trouver des médecins et laboratoires, prendre des rendez-vous en ligne, et accéder à leur dossier médical.
---
## 📋 Table des matières

- [Architecture](#architecture)
- [Stack Technique](#stack-technique)
- [Prérequis](#prérequis)
- [Démarrage rapide avec Docker](#démarrage-rapide-avec-docker)
- [Développement local](#développement-local)
- [Variables d'environnement](#variables-denvironnement)
- [Structure du projet](#structure-du-projet)
- [Fonctionnalités](#fonctionnalités)
- [API](#api)

---
## 🏗️ Architecture

```
Sehati+/
├── .env                    # Variables sensibles (NON commité)
├── .env.example            # Template des variables (à copier)
├── docker-compose.yml      # Orchestration des services
├── sehati/                 # Backend Spring Boot
└── sehati-front/           # Frontend Angular
```

### Services Docker

| Service | Port | Description |
|---|---|---|
| `sehati-backend` | 6060 | API REST Spring Boot |
| `sehati-frontend` | 4200 | Application Angular (nginx) |
| `sehati-mysql` | 3306 | Base de données MySQL 8.0 |
| `sehati-redis` | 6379 | Cache / Sessions |
| `sehati-phpmyadmin` | 8081 | Interface MySQL admin |

---

## 🛠️ Stack Technique

### Backend
- **Java 17** + **Spring Boot 3**
- **Spring Security** + JWT Authentication
- **Spring Data JPA** + MySQL 8.0
- **Spring WebSocket** (STOMP) — Chat temps réel
- **Redis** — Cache & sessions
- **Cloudinary** — Stockage fichiers/images
- **Twilio Verify** — SMS OTP
- **JavaMail** — Notifications email (Gmail SMTP)
- **Groq AI (Whisper)** — Transcription audio

### Frontend
- **Angular 17+** (standalone components)
- **SockJS + STOMP.js** — WebSocket client
- **Google Sign-In** — Authentification OAuth2

---

## ✅ Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) ≥ 24
- [Git](https://git-scm.com/)

Pour le développement local :
- Java 17+
- Maven 3.9+
- Node.js 20+ & npm
- Angular CLI 17+

---

## 🚀 Démarrage rapide avec Docker

### 1. Cloner le projet

```bash
git clone <repo-url>
cd Sehati+
```

### 2. Configurer les variables d'environnement

```bash
# Copier le template
cp .env.example .env

# Remplir les vraies valeurs dans .env
```

> ⚠️ **Le fichier `.env` ne doit JAMAIS être commité.** Il est déjà dans `.gitignore`.

### 3. Lancer tous les services

```bash
docker compose up -d --build
```

### 4. Vérifier que tout tourne

```bash
docker compose ps
```

| URL | Service |
|---|---|
| http://localhost:4200 | Application Angular |
| http://localhost:6060/api | API Backend |
| http://localhost:8081 | phpMyAdmin |

---

## 💻 Développement local

### Backend Spring Boot

**Option A — Depuis IntelliJ IDEA**

Dans la configuration de run, onglet "Environment variables" :
```
SEHATI_APP_JWTSECRET=your_jwt_secret
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_SERVICE_SID=your_service_sid
GROQ_API_KEY=your_groq_key
SEHATI_MAIL_FROM=your_email@gmail.com
```

**Option B — Depuis PowerShell**

```powershell
$env:SEHATI_APP_JWTSECRET="your_jwt_secret"
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
# ... autres variables
cd sehati
mvn spring-boot:run
```

> ℹ️ Les variables `SPRING_DATASOURCE_*` et `SPRING_DATA_REDIS_*` ont des valeurs par défaut (localhost) dans `application.properties`.

### Frontend Angular

```bash
cd sehati-front
npm install
ng serve --port 4200
```

L'application sera disponible sur http://localhost:4200 et communique avec le backend sur `http://localhost:6060`.

---

## 🔐 Variables d'environnement

Copiez `.env.example` en `.env` et renseignez ces valeurs :

### Base de données
| Variable | Description | Exemple |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Mot de passe root MySQL | `strongpassword` |
| `MYSQL_DATABASE` | Nom de la base | `Sehati_db` |
| `SPRING_DATASOURCE_PASSWORD` | Identique à MYSQL_ROOT_PASSWORD | `strongpassword` |

### JWT
| Variable | Description |
|---|---|
| `SEHATI_APP_JWTSECRET` | Clé secrète JWT (min. 32 caractères) |
| `SEHATI_APP_JWTEXPIRATIONMS` | Durée de validité en ms (ex: `86400000` = 24h) |

### Email SMTP
| Variable | Description |
|---|---|
| `SPRING_MAIL_USERNAME` | Adresse Gmail expéditrice |
| `SPRING_MAIL_PASSWORD` | [App Password Gmail](https://myaccount.google.com/apppasswords) |
| `SEHATI_MAIL_FROM` | Adresse affichée dans les emails |

### Cloudinary
| Variable | Description |
|---|---|
| `CLOUDINARY_CLOUD_NAME` | Nom du cloud (dashboard Cloudinary) |
| `CLOUDINARY_API_KEY` | Clé API |
| `CLOUDINARY_API_SECRET` | Secret API |

### Twilio (SMS OTP)
| Variable | Description |
|---|---|
| `TWILIO_ACCOUNT_SID` | Account SID (console Twilio) |
| `TWILIO_AUTH_TOKEN` | Auth Token |
| `TWILIO_SERVICE_SID` | Verify Service SID |

### Groq AI
| Variable | Description |
|---|---|
| `GROQ_API_KEY` | Clé API [Groq](https://console.groq.com/) |

---

## 📁 Structure du projet

```
sehati/                             # Backend Spring Boot
├── src/main/java/com/sehati/
│   ├── admin/                      # Module Administration
│   ├── ai/                         # Module IA (transcription audio)
│   ├── appointment/                # Module Rendez-vous
│   ├── auth/                       # Authentification JWT + Google OAuth
│   ├── chat/                       # Chat temps réel WebSocket
│   ├── common/                     # Services partagés (email, SMS, Cloudinary)
│   ├── config/                     # Configuration Spring (Security, Redis, etc.)
│   ├── laboratoire/                # Module Laboratoires
│   ├── medecin/                    # Module Médecins
│   ├── notification/               # Rappels & notifications
│   ├── patient/                    # Module Patients
│   └── secretaire/                 # Module Secrétaires
└── src/main/resources/
    └── application.properties      # Config (lit les variables d'environnement)

sehati-front/                       # Frontend Angular
├── src/
│   ├── app/
│   │   ├── admin/                  # Dashboard administrateur
│   │   ├── auth/                   # Login, inscription, setup secrétaire
│   │   ├── core/                   # Services, modèles, guards
│   │   ├── medecin/                # Dashboard médecin
│   │   ├── patient/                # Profil & historique patient
│   │   └── shared/                 # Composants réutilisables (navbar, sidebar...)
│   └── environments/
│       ├── environment.ts          # Config développement
│       └── environment.prod.ts     # Config production
```

---

## 🎯 Fonctionnalités

### 👤 Patient
- Inscription & connexion (email/password + Google OAuth)
- Recherche de médecins et laboratoires par ville/spécialité
- Prise de rendez-vous en ligne
- Historique médical & résultats d'analyses
- Notifications email (rappels, annulations, résultats)
- Vérification téléphone par SMS OTP

### 🩺 Médecin
- Gestion du planning et des rendez-vous
- Génération d'ordonnances PDF
- Génération de demandes d'analyses
- Chat temps réel avec la secrétaire
- Dashboard statistiques
- Gestion de la secrétaire associée

### 🔬 Laboratoire
- Gestion des rendez-vous d'analyses
- Upload des résultats
- Dashboard statistiques

### 👩‍💼 Secrétaire
- Gestion des rendez-vous du médecin associé
- Chat temps réel avec le médecin
- Création manuelle de rendez-vous

### 🛡️ Administrateur
- Gestion des utilisateurs (activation/désactivation)
- Validation des demandes d'inscription médecins/labos
- Dashboard statistiques global
- Gestion des messages de support

---

## 🔌 API

La documentation de l'API REST est accessible depuis :

```
http://localhost:6060/swagger-ui.html  
```

### Endpoints principaux

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/signin` | Connexion |
| `POST` | `/api/auth/signup/patient` | Inscription patient |
| `POST` | `/api/auth/signup/medecin` | Inscription médecin |
| `GET` | `/api/public/medecins` | Liste publique des médecins |
| `GET` | `/api/public/labos` | Liste publique des laboratoires |
| `POST` | `/api/appointments` | Créer un rendez-vous |
| `GET` | `/api/appointments/my-upcoming` | Mes prochains RDV |
| `GET` | `/api/admin/dashboard/overview` | Stats admin |
| `WS` | `/ws` | WebSocket (chat temps réel) |

---

## 🔒 Sécurité

- Toutes les clés sensibles sont stockées dans `.env` (jamais dans le code)
- Authentification JWT Bearer token
- Autorisation par rôle (`PATIENT`, `MEDECIN`, `LABORATOIRE`, `SECRETAIRE`, `ADMIN`)
- HTTPS recommandé en production
- `.env` exclu du versioning Git (vérifié dans `.gitignore`)

---

## 📝 Licence

Projet académique — Sehati+ © 2026
