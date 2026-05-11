# Configuration Docker pour MineHost

## Vue d'ensemble

Ce projet a été modifié pour utiliser Docker Compose et créer des containers Docker dédiés pour chaque monde Minecraft au lieu d'utiliser `screen`.

## Changements effectués

### 1. Dépendances ajoutées
- `docker-java`: Client Java pour interagir avec Docker
- `docker-java-transport-httpclient5`: Transport HTTP pour docker-java

### 2. Nouveaux fichiers créés

#### DockerService.java
Service Spring pour gérer les containers Docker:
- `startWorldContainer()`: Crée et démarre un container pour un monde
- `stopWorldContainer()`: Arrête et supprime un container
- `isContainerRunning()`: Vérifie si un container est en cours d'exécution
- `getContainerLogs()`: Récupère les logs du container

#### Dockerfile (mondes)
Dockerfile pour créer l'image `minecraft-world:latest`:
- Base: Ubuntu 22.04
- Outils: Java 21, ngrok
- Point d'entrée: `entrypoint.sh`

#### entrypoint.sh
Script d'initialisation pour le container du monde:
- Démarre le serveur Minecraft
- Lance ngrok pour l'exposition en tunnel
- Génère un fichier `ngrok_url.txt` avec l'URL d'accès

### 3. Modifications existantes

#### WorldService.java
- Intégration du `DockerService`
- `startWorld()` et `startWorldTT()` utilisent Docker au lieu de `screen`
- `stopWorld()` utilise Docker au lieu de `screen`

#### docker-compose.yml
- Ajout du volume `/var/run/docker.sock` pour l'accès à Docker
- Le container `minehost` peut maintenant créer des containers enfants

#### Dockerfile (principal)
- Ajout de `docker.io` (Docker CLI)
- Copie du Dockerfile et du script d'entrypoint pour les mondes

#### application.properties
- Configuration pour le socket Docker
- Configuration pour l'image et le réseau Docker

## Instructions de construction de l'image des mondes

Avant de lancer le projet, construisez l'image Docker pour les mondes:

```bash
# Se placer à la racine du projet
cd /path/to/MineHost

# Construire l'image minecraft-world
docker build -f backend/mondes/Dockerfile -t minecraft-world:latest backend/mondes/

# Lancer le projet avec Docker Compose
docker compose up -d --build
```

## Structure des containers

Chaque monde crée un container nommé `world_<worldId>` avec:
- Volume monté: `/chemin/vers/monde:/world`
- Port exposé: `<port>:25565` pour le serveur Minecraft
- Port exposé: `<port+1>:<port+1>` pour RCON
- Réseau: `minehost-network`

## Avantages de cette approche

1. **Isolation**: Chaque monde s'exécute dans son propre container
2. **Scalabilité**: Facile d'ajouter plusieurs mondes simultanément
3. **Gestion ressources**: Chaque container peut être limité en ressources CPU/RAM
4. **Nettoyage**: Arrêt facile d'un monde = suppression du container
5. **Logs**: Logs des containers facilement accessibles
6. **Ngrok intégré**: Chaque monde peut être exposé automatiquement

## Troubleshooting

### Erreur: "Cannot connect to Docker daemon"
- Vérifiez que le socket Docker est monté: `/var/run/docker.sock`
- Vérifiez les permissions du socket

### Erreur: "Image not found: minecraft-world:latest"
- Construisez l'image en exécutant:
  ```bash
  docker build -f backend/mondes/Dockerfile -t minecraft-world:latest backend/mondes/
  ```

### Container ne démarre pas
- Vérifiez les logs: `docker logs world_<worldId>`
- Vérifiez que le script `<worldName>.sh` existe et est exécutable
- Vérifiez que les ports ne sont pas déjà utilisés
