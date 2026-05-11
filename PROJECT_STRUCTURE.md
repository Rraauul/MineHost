# 📦 Arborescence complète - MineHost avec Docker

```
MineHost/
│
├── 🚀 Scripts de démarrage
│   ├── build.sh                    # Script de build Linux/Mac
│   └── build.ps1                   # Script de build Windows
│
├── 🐳 Configuration Docker
│   ├── docker-compose.yml          # Orchestration des services
│   └── .env.example               # Template de configuration
│
├── 📄 Documentation
│   ├── README.md                   # Vue d'ensemble du projet
│   ├── QUICKSTART.md               # Guide de démarrage rapide (⭐ Commencer ici)
│   ├── DOCKER_SETUP.md             # Configuration Docker détaillée
│   ├── ENV_GUIDE.md                # Guide des variables d'environnement
│   ├── CHANGES_SUMMARY.md          # Résumé des modifications
│   └── PROJECT_STRUCTURE.md        # Ce fichier
│
├── 📁 backend/
│   │
│   ├── pom.xml                     # Configuration Maven (+ dépendances Docker)
│   │
│   ├── Dockerfile                  # Image Docker pour l'app Spring
│   │                               # (+ Docker CLI, + docker-java)
│   │
│   ├── mvnw / mvnw.cmd            # Maven wrapper
│   │
│   ├── src/
│   │   └── main/
│   │       ├── java/mineHost/
│   │       │   ├── 🆕 service/
│   │       │   │   ├── DockerService.java        # 🆕 Gestion Docker
│   │       │   │   ├── WorldService.java         # ✏️ Modifié: Docker intégré
│   │       │   │   └── ServerService.java
│   │       │   │
│   │       │   ├── controller/
│   │       │   │   ├── WorldController.java
│   │       │   │   └── ServerController.java
│   │       │   │
│   │       │   ├── model/
│   │       │   │   ├── World.java
│   │       │   │   ├── Server.java
│   │       │   │   └── User.java
│   │       │   │
│   │       │   ├── repository/
│   │       │   │   ├── WorldRepository.java
│   │       │   │   ├── ServerRepository.java
│   │       │   │   └── UserRepository.java
│   │       │   │
│   │       │   ├── dto/
│   │       │   │   ├── WorldDTO.java
│   │       │   │   ├── ServerDTO.java
│   │       │   │   └── UserDTO.java
│   │       │   │
│   │       │   └── Main.java
│   │       │
│   │       └── resources/
│   │           └── application.properties        # ✏️ Modifié: Config Docker
│   │
│   └── mondes/
│       │
│       ├── 🆕 Dockerfile                   # Image pour les serveurs Minecraft
│       ├── 🆕 entrypoint.sh               # Script de démarrage du serveur
│       │
│       ├── Templates/
│       │   └── TemplateSimpleMC/          # Template de serveur Minecraft
│       │       ├── config/
│       │       │   ├── *.toml             # Config des mods
│       │       │   └── ...
│       │       │
│       │       ├── mods/                  # Mods Minecraft
│       │       │
│       │       ├── server.properties      # Config du serveur
│       │       ├── start.sh              # Script de démarrage
│       │       ├── eula.txt              # Accord de licence
│       │       ├── user_jvm_args.txt     # Arguments JVM
│       │       └── ...
│       │
│       ├── user_*/                       # Dossiers des utilisateurs (créés dynamiquement)
│       │   └── MondeNom/                # Dossier de chaque monde (crée son container)
│       │       ├── MondeNom.sh          # Script de démarrage renommé
│       │       ├── server.properties    # Propriétés du serveur
│       │       ├── ngrok.log            # Logs ngrok
│       │       ├── ngrok_url.txt        # URL publique ngrok
│       │       ├── config/              # Config des mods
│       │       ├── logs/                # Logs du serveur
│       │       └── ...
│       │
│       └── Outputs/                     # Données générées
│
└── 📋 Fichiers de configuration
    ├── .gitignore                        # Ignore .env et autres fichiers sensibles
    └── .env                              # ⚠️ À créer depuis .env.example

```

## 🔄 Flux de données - Démarrage d'un monde

```
┌─────────────────┐
│  Utilisateur    │
└────────┬────────┘
         │ POST /api/worlds/{id}/start
         ▼
┌──────────────────────────┐
│  WorldController         │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────┐
│  WorldService            │ ◄── Injecté avec DockerService
│  ✏️ startWorld()          │
└────────┬─────────────────┘
         │ Appelle
         ▼
┌──────────────────────────┐
│  DockerService           │ ◄── 🆕 Service Docker
│  🆕 startWorldContainer()│
└────────┬─────────────────┘
         │ Crée
         ▼
┌──────────────────────────┐
│  Docker Engine           │
└────────┬─────────────────┘
         │ Lance image
         ▼
┌──────────────────────────┐
│  Container: world_{id}   │
│ ┌──────────────────────┐ │
│ │ entrypoint.sh        │ │
│ │ ├─ Démarre serveur   │ │
│ │ └─ Démarre ngrok     │ │
│ └──────────────────────┘ │
└──────────────────────────┘
         │ Génère
         ▼
    ngrok_url.txt
    (URL publique)
```

## 📊 Architecture Docker

```
┌────────────────────────────────────────────────┐
│         Host Machine                           │
│                                                │
│ ┌─────────────────────────────────────────┐   │
│ │ Docker Engine                           │   │
│ │                                         │   │
│ │ ┌──────────────────────────────────┐   │   │
│ │ │ Network: minehost-network       │   │   │
│ │ │                                 │   │   │
│ │ │ ┌──────────┐  ┌──────────┐     │   │   │
│ │ │ │ mysql    │  │ minehost │     │   │   │
│ │ │ │ :3306    │  │ :8080    │     │   │   │
│ │ │ │          │  │          │     │   │   │
│ │ │ │ MySQL    │  │ Spring   │     │   │   │
│ │ │ │ Database │  │ Boot App │     │   │   │
│ │ │ └──────────┘  └────┬─────┘     │   │   │
│ │ │                    │            │   │   │
│ │ │   ┌─────────────────┘            │   │   │
│ │ │   │ Crée dynamiquement           │   │   │
│ │ │   ▼                              │   │   │
│ │ │ ┌────────────────────────────┐  │   │   │
│ │ │ │ Container: world_1         │  │   │   │
│ │ │ │ ├─ Port 25565 (Minecraft) │  │   │   │
│ │ │ │ ├─ Serveur Minecraft      │  │   │   │
│ │ │ │ └─ ngrok (tunnel)        │  │   │   │
│ │ │ └────────────────────────────┘  │   │   │
│ │ │                                  │   │   │
│ │ │ ┌────────────────────────────┐  │   │   │
│ │ │ │ Container: world_2         │  │   │   │
│ │ │ │ ├─ Port 25566 (Minecraft) │  │   │   │
│ │ │ │ └─ Serveur Minecraft      │  │   │   │
│ │ │ └────────────────────────────┘  │   │   │
│ │ │                                  │   │   │
│ │ │ ┌────────────────────────────┐  │   │   │
│ │ │ │ Container: world_N         │  │   │   │
│ │ │ │ └─ ...                     │  │   │   │
│ │ │ └────────────────────────────┘  │   │   │
│ │ │                                 │   │   │
│ │ └──────────────────────────────────┘   │   │
│ │                                         │   │
│ │ Socket: /var/run/docker.sock            │   │
│ │ (Accessible par minehost container)    │   │
│ │                                         │   │
│ └─────────────────────────────────────────┘   │
│                                                │
│ ┌─────────────────────────────────────────┐   │
│ │ Volumes                                 │   │
│ │ ├── ./mondes:/home/fuentesr/mondes     │   │
│ │ └── /var/run/docker.sock               │   │
│ └─────────────────────────────────────────┘   │
└────────────────────────────────────────────────┘
```

## 🎯 Points clés par fichier

### Nouveaux fichiers (🆕)

| Fichier | Responsabilité | Clé |
|---------|---|---|
| `DockerService.java` | Gestion des containers Docker | ✅ Service injectable |
| `Dockerfile` (mondes) | Image des serveurs Minecraft | ✅ Support ngrok intégré |
| `entrypoint.sh` | Démarrage du serveur | ✅ Automatisation complète |
| `build.sh / .ps1` | Déploiement automatisé | ✅ Commandes simplifiées |
| `DOCKER_SETUP.md` | Documentation Docker | ✅ Troubleshooting |
| `QUICKSTART.md` | Démarrage rapide | ✅ Guide complet |
| `ENV_GUIDE.md` | Variables d'environnement | ✅ Configuration détaillée |
| `CHANGES_SUMMARY.md` | Résumé des modifications | ✅ Migration de screen → Docker |

### Fichiers modifiés (✏️)

| Fichier | Changements | Impact |
|---------|---|---|
| `pom.xml` | + docker-java, docker-java-transport | ✅ Dépendances Docker |
| `WorldService.java` | startWorld() + DockerService | ✅ Logique entièrement Docker |
| `Dockerfile` | + docker.io + copies | ✅ Capable de créer containers |
| `docker-compose.yml` | + socket Docker | ✅ Communication Docker possible |
| `application.properties` | + config Docker | ✅ Paramétrage centralisé |
| `README.md` | Documentation mise à jour | ✅ Instructions claires |

## 🔐 Fichiers sensibles

⚠️ **Ne pas commiter**:
- `.env` - Contient les mots de passe
- `docker-compose.override.yml` - Overrides locaux

✅ **À commiter**:
- `.env.example` - Template pour les utilisateurs
- Tous les autres fichiers

## 📝 Ordre de visite recommandé

Pour comprendre le projet:

1. **QUICKSTART.md** - Démarrer en 5 minutes
2. **README.md** - Vue d'ensemble
3. **CHANGES_SUMMARY.md** - Comprendre les modifications
4. **DOCKER_SETUP.md** - Configuration approfondie
5. **ENV_GUIDE.md** - Variables et configuration
6. **Code Java** - Logique métier

## 🚀 Commands utiles

```bash
# Navigation
cd MineHost
cd backend
cd backend/mondes

# Docker
docker compose up -d
docker compose logs -f
docker ps
docker logs world_1
docker exec -it minehost bash

# Maven
mvn clean package -DskipTests
mvn spring-boot:run

# Git
git add -A
git commit -m "Add Docker support"
git push
```

## 📦 Dépendances principales

```
Spring Boot 3.2.3
├── spring-boot-starter-data-jpa
├── spring-boot-starter-web
├── spring-security-core
└── mysql-connector-j

docker-java 3.3.3
├── docker-java (API)
└── docker-java-transport-httpclient5 (Transport)

Autres
├── lombok
└── junit (tests)
```

---

**Version**: 1.0 | **Date**: 2026-05-11 | **Auteur**: GitHub Copilot

Pour poser des questions, veuillez consulter la documentation ou ouvrir une issue.
