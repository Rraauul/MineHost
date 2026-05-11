# MineHost

Plateforme de gestion de serveurs Minecraft avec isolation par Docker.

## État du projet

### ✓ Complété
- Fusion du backend
- Utilisation de Java 21
- Utilisation de Docker Compose
- Création du fichier `.env`
- **Nouveau**: Isolation des mondes en containers Docker
- **Nouveau**: Support de ngrok pour l'exposition automatique des serveurs

### 🔄 En cours
- Utilisation de chemins relatifs pour compatibilité locale/cloud
- Interface client

### 📋 À faire
- Fusion du client
- Panel d'administration web

## Démarrage rapide

### Prérequis
- Docker et Docker Compose
- Git
- (Optionnel) Java 21 pour le développement local

### Installation

1. **Cloner le projet**
   ```bash
   git clone <repository>
   cd MineHost
   ```

2. **Créer le fichier `.env`**
   ```bash
   cp .env.example .env
   # Éditer .env avec vos configurations
   ```

3. **Construire l'image Docker des mondes**
   ```bash
   # Sous Windows (PowerShell)
   .\build.ps1
   
   # Sous Linux/Mac
   bash build.sh
   ```

4. **Accéder à l'API**
   - API MineHost: `http://localhost:8080`
   - MySQL: `localhost:3308`

## Architecture Docker

### Services
- **minehost**: Application Spring Boot
- **mysql**: Base de données MySQL
- **world_***: Containers des mondes Minecraft (créés dynamiquement)

### Réseau
- **minehost-network**: Réseau bridge interne pour tous les services

### Volumes
- `/home/fuentesr/mondes`: Partage des mondes entre l'hôte et les containers

## Utilisation

### Créer un monde
```
POST /api/worlds
{
  "name": "MonMonde",
  "template": "TemplateSimpleMC",
  "ram": 4,
  "userId": 1,
  "serverId": 1
}
```

### Démarrer un monde
```
POST /api/worlds/{worldId}/start
```
Cela crée automatiquement un container Docker pour le monde.

### Arrêter un monde
```
POST /api/worlds/{worldId}/stop
```
Cela arrête et supprime le container Docker.

### Récupérer les logs
```
GET /api/worlds/{worldId}/logs
```

## Structure du projet

```
MineHost/
├── backend/
│   ├── src/
│   │   ├── main/java/mineHost/
│   │   │   ├── service/
│   │   │   │   ├── WorldService.java
│   │   │   │   ├── DockerService.java
│   │   │   │   └── ServerService.java
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── dto/
│   │   └── resources/
│   │       └── application.properties
│   ├── mondes/
│   │   ├── Templates/
│   │   │   └── TemplateSimpleMC/
│   │   ├── Dockerfile (pour les mondes)
│   │   └── entrypoint.sh
│   ├── Dockerfile (application principale)
│   └── pom.xml
├── docker-compose.yml
├── build.sh / build.ps1
├── DOCKER_SETUP.md
└── README.md
```

## Configuration

### Environment Variables (.env)
```
MYSQL_DB_USERNAME=minehost
MYSQL_DB_PASSWORD=password
MYSQL_DATABASE=minehost
NGROK_TOKEN=your_ngrok_token
DATABASE_URL=jdbc:mysql://mysql:3306/minehost
```

### application.properties
```properties
docker.host=unix:///var/run/docker.sock
docker.world.image=minecraft-world:latest
docker.world.network=minehost-network
```

## Dépendances principales

- **Spring Boot 3.2.3**: Framework web
- **MySQL Connector**: Driver JDBC
- **docker-java 3.3.3**: Client Docker
- **Lombok**: Génération de code
- **Spring Security**: Authentification

## Commandes utiles

```bash
# Voir les logs du service minehost
docker compose logs -f minehost

# Voir les logs d'un monde spécifique
docker logs world_1

# Voir tous les containers
docker ps

# Arrêter tous les services
docker compose down

# Supprimer tous les containers et volumes
docker compose down -v

# Reconstruire les images
docker compose up -d --build
```

## Troubleshooting

Voir [DOCKER_SETUP.md](DOCKER_SETUP.md) pour les problèmes courants.

## Documentation additionnelle

- [Configuration Docker détaillée](DOCKER_SETUP.md)
- [API Documentation](docs/api.md) (à créer)

## Auteur

MineHost Team

## Licence

À définir
