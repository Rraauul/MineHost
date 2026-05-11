# Guide de démarrage rapide - MineHost avec Docker

## 📋 Prérequis

- ✓ Docker Desktop (Windows/Mac) ou Docker + Docker Compose (Linux)
- ✓ Git
- ✓ Au minimum 4GB de RAM disponible

## 🚀 Démarrage en 5 minutes

### Étape 1: Cloner et configurer
```bash
git clone <votre-repo>
cd MineHost
cp .env.example .env
```

### Étape 2: Éditer .env (optionnel mais recommandé)
```bash
# Changez au moins le mot de passe MySQL
MYSQL_DB_PASSWORD=votre_mot_de_passe_securise

# (Optionnel) Ajoutez votre token ngrok pour l'exposition des serveurs
NGROK_TOKEN=votre_token_ici
```

### Étape 3: Lancer le build et déploiement

#### Sur Windows (PowerShell)
```powershell
.\build.ps1
```

#### Sur Linux/Mac
```bash
bash build.sh
```

### Étape 4: Attendre le démarrage
```bash
# Vérifier l'état
docker compose ps

# Voir les logs (Ctrl+C pour quitter)
docker compose logs -f minehost
```

### Étape 5: Utiliser l'API

L'API est disponible à: **http://localhost:8080**

```bash
# Exemple: Créer un monde
curl -X POST http://localhost:8080/api/worlds \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MonMonde",
    "template": "TemplateSimpleMC",
    "ram": 4,
    "userId": 1,
    "serverId": 1
  }'

# Démarrer le monde (worldId = 1)
curl -X POST http://localhost:8080/api/worlds/1/start

# Arrêter le monde
curl -X POST http://localhost:8080/api/worlds/1/stop
```

## 📊 Vérification de l'installation

```bash
# Voir tous les services
docker compose ps

# Vérifier les containers des mondes
docker ps -a | grep world_

# Voir les logs d'un monde (world_1)
docker logs world_1

# Voir les logs du service principal
docker compose logs minehost

# Accéder à MySQL (si nécessaire)
docker exec -it mysql mysql -u minehost -p
```

## 🛑 Arrêter les services

```bash
# Arrêter tous les services
docker compose down

# Arrêter et supprimer les volumes (données)
docker compose down -v

# Arrêter un monde spécifique
docker stop world_1
docker rm world_1
```

## 🔍 Dépannage rapide

### L'API ne démarre pas
```bash
# Vérifier les erreurs de compilation
docker compose build --no-cache minehost
docker compose logs minehost
```

### L'image minecraft-world n'existe pas
```bash
# Reconstruire l'image
docker build -f backend/mondes/Dockerfile -t minecraft-world:latest backend/mondes/
```

### Les ports sont déjà utilisés
```bash
# Vérifier les ports occupés
netstat -an | grep LISTEN  # Linux/Mac
netstat -ano | findstr LISTEN  # Windows
```

### Problèmes de permissions Docker
```bash
# Linux seulement
sudo usermod -aG docker $USER
newgrp docker
```

## 📁 Fichiers importants

| Fichier | Description |
|---------|------------|
| `.env` | Configuration des services (créer depuis `.env.example`) |
| `docker-compose.yml` | Orchestration des containers |
| `backend/Dockerfile` | Image de l'application Spring |
| `backend/mondes/Dockerfile` | Image des serveurs Minecraft |
| `backend/mondes/entrypoint.sh` | Script de démarrage du serveur |
| `build.sh / build.ps1` | Scripts de déploiement automatisés |

## 💡 Conseils utiles

1. **Variables d'environnement**: Toutes les config sont dans `.env`
2. **Logs en temps réel**: `docker compose logs -f minehost`
3. **Rebuild sans cache**: `docker compose up --build --no-cache`
4. **Accès au shell du container**: `docker exec -it minehost bash`
5. **Monitoring**: `docker stats` pour voir l'utilisation des ressources

## 📚 Documentation complète

- [README.md](README.md) - Aperçu du projet
- [DOCKER_SETUP.md](DOCKER_SETUP.md) - Configuration Docker détaillée
- [CHANGES_SUMMARY.md](CHANGES_SUMMARY.md) - Résumé des modifications

## ✅ Checklist post-installation

- [ ] Docker et Docker Compose installés
- [ ] `.env` créé et configuré
- [ ] `build.sh` ou `build.ps1` exécuté sans erreurs
- [ ] `docker compose ps` montre tous les services
- [ ] API répond sur `http://localhost:8080`
- [ ] MySQL connecté (port 3308)
- [ ] Premiers mondes créés et démarrés avec succès

## 🆘 Support

Pour les problèmes:
1. Consulter [DOCKER_SETUP.md](DOCKER_SETUP.md) section Troubleshooting
2. Vérifier les logs: `docker compose logs -f`
3. Vérifier que les ports ne sont pas utilisés
4. Vérifier la connexion Docker: `docker ps`

---

**Besoin d'aide?** Consultez la documentation complète ou ouvrez une issue.
