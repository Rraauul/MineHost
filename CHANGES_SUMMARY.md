# Résumé des modifications - Intégration Docker

## 📝 Vue d'ensemble

Rénovation majeure de la méthode `startWorld()` et `startWorldTT()` pour utiliser Docker au lieu de `screen`. Chaque monde Minecraft s'exécute maintenant dans son propre container Docker isolé avec support intégré de ngrok.

## 🔄 Changements principaux

### 1. Nouvelles dépendances (pom.xml)
```xml
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java</artifactId>
    <version>3.3.3</version>
</dependency>
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-transport-httpclient5</artifactId>
    <version>3.3.3</version>
</dependency>
```

### 2. Nouveau service: DockerService.java
**Localisation**: `backend/src/main/java/mineHost/service/DockerService.java`

**Responsabilités**:
- Gestion du cycle de vie des containers Docker
- Création de containers avec configurations appropriées
- Arrêt et suppression de containers
- Vérification du statut des containers
- Récupération des logs

**Méthodes principales**:
- `startWorldContainer()`: Lance un container pour un monde
- `stopWorldContainer()`: Arrête et supprime un container
- `isContainerRunning()`: Vérifie le statut
- `getContainerLogs()`: Récupère les logs

### 3. Modifié: WorldService.java
**Changements**:
- Ajout de la dépendance `DockerService` via injection
- Remplacement de `screen` par Docker dans `startWorld()`
- Remplacement de `screen` par Docker dans `startWorldTT()`
- Simplification de `stopWorld()` via Docker

**Avant (avec screen)**:
```java
ProcessBuilder processBuilder = new ProcessBuilder("screen", "-dmS", "world_" + worldId + "sh",
        "bash", scriptFile.getAbsolutePath());
```

**Après (avec Docker)**:
```java
String containerId = dockerService.startWorldContainer(
    worldId,
    world.getName(),
    worldDir.getAbsolutePath(),
    port
);
```

### 4. Nouveau: Dockerfile pour les mondes
**Localisation**: `backend/mondes/Dockerfile`

**Contenu**:
- Base: Ubuntu 22.04
- Outils: Java 21, ngrok, curl
- Script d'entrypoint automatisé
- Support des volumes pour les données du monde
- Exposition des ports 25565 (serveur) et port+1 (RCON)

### 5. Nouveau: Script d'entrypoint
**Localisation**: `backend/mondes/entrypoint.sh`

**Fonctionnalités**:
- Démarre le serveur Minecraft
- Initialise ngrok avec un token
- Génère un fichier `ngrok_url.txt` avec l'URL d'accès
- Maintient le container actif

### 6. Modifié: Dockerfile principal (backend)
**Changements**:
- Ajout de `docker.io` pour l'accès au client Docker
- Copie du Dockerfile et du script d'entrypoint pour les mondes
- Configuration pour exécuter les commandes Docker
- Ajout du groupe `docker` à l'utilisateur `fuentesr`

### 7. Modifié: docker-compose.yml
**Changements**:
```yaml
volumes:
  - ./mondes:/home/fuentesr/mondes
  - /var/run/docker.sock:/var/run/docker.sock  # ← NOUVEAU
```

Permet au container `minehost` de créer et gérer d'autres containers.

### 8. Modifié: application.properties
**Ajouts**:
```properties
docker.host=unix:///var/run/docker.sock
docker.world.image=minecraft-world:latest
docker.world.network=minehost-network
```

### 9. Nouveaux fichiers de configuration

#### .env.example
- Exemple de fichier de configuration
- Variables pour MySQL, ngrok, et Docker

#### build.sh et build.ps1
- Scripts d'automatisation
- Construction de l'image Docker des mondes
- Démarrage avec docker-compose

#### DOCKER_SETUP.md
- Documentation détaillée sur Docker
- Guide de troubleshooting
- Instructions de construction manuelle

#### Modifications README.md
- Documentation mise à jour
- Instructions de démarrage
- Structure du projet
- Commandes utiles

## 🚀 Processus de lancement

### Avant (avec screen)
1. Exécute un script shell via `ProcessBuilder`
2. Lance ngrok en tant que processus séparé
3. Gère deux `screen` sessions indépendamment
4. Difficile de monitorer et de nettoyer les processus

### Après (avec Docker)
1. Crée un container Docker isolé
2. Monte le volume du monde
3. Expose les ports configurés
4. Exécute le script d'entrypoint automatiquement
5. ngrok intégré dans le container
6. Nettoyage facile: une commande pour arrêter

## 📦 Structure des containers créés

```
Container: world_<worldId>
├── Image: minecraft-world:latest
├── Volumes:
│   └── /path/to/world:/world
├── Ports:
│   ├── <port>:25565 (Serveur Minecraft)
│   └── <port+1>:<port+1> (RCON)
├── Network: minehost-network
└── Env:
    ├── WORLD_NAME=<name>
    ├── SERVER_PORT=<port>
    └── NGROK_TOKEN=<token>
```

## ✅ Avantages de cette implémentation

| Aspect | Avant | Après |
|--------|-------|-------|
| **Isolation** | Partage les processus du système | Container isolé |
| **Ressources** | Pas de limite | Limitable par container |
| **Nettoyage** | Manuel, peut laisser des orphelins | Automatique avec suppression |
| **Logs** | Via fichiers ou `screen` | Logs Docker centralisés |
| **Scalabilité** | Difficile avec plusieurs mondes | Facile, un container = un monde |
| **Portabilité** | Dépendant du système hôte | Docker rend portable |
| **ngrok** | Processus séparé | Intégré au container |

## 🔧 Installation et déploiement

```bash
# 1. Clone du projet
git clone <repository>
cd MineHost

# 2. Configuration .env
cp .env.example .env
# Éditer .env avec vos paramètres

# 3. Construction (Windows)
.\build.ps1

# OU (Linux/Mac)
bash build.sh

# 4. Vérification
docker compose ps
docker logs world_1  # Pour un monde spécifique
```

## 🐛 Points importants pour le développement

1. **Image minecraft-world:latest**: Doit être construite avant le premier démarrage
2. **Socket Docker**: Le container minehost doit accéder à `/var/run/docker.sock`
3. **Permissions Docker**: L'utilisateur doit être dans le groupe `docker`
4. **Réseau**: Les containers utilisent le réseau `minehost-network`
5. **Volumes**: Les données du monde sont persistantes via bind mount

## 📋 Checklist de vérification

- [ ] Dépendances Maven ajoutées
- [ ] DockerService créé et injectable
- [ ] WorldService modifié et compilé
- [ ] Dockerfile des mondes créé
- [ ] entrypoint.sh créé et exécutable
- [ ] docker-compose.yml mis à jour
- [ ] application.properties configuré
- [ ] build.sh et build.ps1 fonctionnels
- [ ] .env.example créé
- [ ] Documentation mise à jour

## 🚨 Erreurs courantes et solutions

### Erreur: "Cannot connect to Docker daemon"
**Cause**: Socket Docker non disponible ou permissions insuffisantes
**Solution**: 
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### Erreur: "Image minecraft-world:latest not found"
**Cause**: Image Docker non construite
**Solution**: `docker build -f backend/mondes/Dockerfile -t minecraft-world:latest backend/mondes/`

### Container ne démarre pas
**Cause**: Script de lancement manquant ou ports en conflit
**Solution**: 
```bash
docker logs world_<id>  # Voir les erreurs
docker ps -a            # Vérifier le container
```

## 📚 Fichiers affectés

### Modifiés
- `backend/pom.xml` - Dépendances Docker ajoutées
- `backend/src/main/java/mineHost/service/WorldService.java` - Logique Docker intégrée
- `backend/Dockerfile` - Docker CLI ajouté
- `docker-compose.yml` - Socket Docker monté
- `backend/src/main/resources/application.properties` - Config Docker
- `README.md` - Documentation mise à jour

### Créés
- `backend/src/main/java/mineHost/service/DockerService.java` - Service Docker
- `backend/mondes/Dockerfile` - Image pour les mondes
- `backend/mondes/entrypoint.sh` - Script de démarrage
- `build.sh` - Script de build Linux
- `build.ps1` - Script de build Windows
- `DOCKER_SETUP.md` - Documentation Docker
- `.env.example` - Exemple de configuration
- `CHANGES_SUMMARY.md` - Ce fichier

## 🎯 Prochaines étapes possibles

1. **Healthchecks**: Ajouter des vérifications de santé du container
2. **Limites de ressources**: Implémenter les limites CPU/RAM
3. **Monitoring**: Intégrer Prometheus/Grafana
4. **Auto-scaling**: Support des réplicas de monde
5. **Orchestration**: Passer à Kubernetes pour la prod
6. **Backup**: Sauvegardes automatiques des mondes
7. **Snapshots**: Support des snapshots/points de restauration

---

**Date de mise à jour**: 2026-05-11
**Auteur**: GitHub Copilot
**Version**: 1.0