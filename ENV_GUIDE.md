# Variables d'environnement et configuration

## Vue d'ensemble

Le fichier `.env` contient toutes les variables de configuration nécessaires pour exécuter MineHost avec Docker Compose.

## Variables disponibles

### 📦 Configuration MySQL

| Variable | Description | Exemple | Obligatoire |
|----------|------------|---------|------------|
| `MYSQL_DB_USERNAME` | Nom d'utilisateur MySQL | `minehost` | ✓ |
| `MYSQL_DB_PASSWORD` | Mot de passe MySQL | `secure_password` | ✓ |
| `MYSQL_DATABASE` | Nom de la base de données | `minehost` | ✓ |

**Remarque**: Ces identifiants sont utilisés uniquement en interne. N'oubliez pas de modifier le mot de passe de celui par défaut.

### 🌐 Configuration ngrok

| Variable | Description | Exemple | Obligatoire |
|----------|------------|---------|------------|
| `NGROK_TOKEN` | Token d'authentification ngrok | `3_1234567890...` | ✗ |

**Où l'obtenir**:
1. Créer un compte sur [ngrok.com](https://ngrok.com)
2. Accéder à [Dashboard](https://dashboard.ngrok.com/get-started/your-authtoken)
3. Copier le token et le coller dans `.env`

**Avantages de ngrok**:
- Accès aux serveurs Minecraft depuis l'extérieur
- URL publique stable pour chaque serveur
- Utile pour les tests et les amis distants

### 🗄️ Configuration Base de données

| Variable | Description | Exemple | Obligatoire |
|----------|------------|---------|------------|
| `DATABASE_URL` | URL JDBC pour la base | `jdbc:mysql://mysql:3306/minehost` | ✓ |

**Note**: Cette URL utilise le hostname `mysql` qui est le nom du service Docker. N'y touchez que si vous savez ce que vous faites.

### 🐳 Configuration Docker (avancée)

| Variable | Description | Défaut | Obligatoire |
|----------|------------|--------|------------|
| `DOCKER_HOST` | Socket Docker | `unix:///var/run/docker.sock` | ✗ |
| `DOCKER_WORLD_IMAGE` | Image pour les mondes | `minecraft-world:latest` | ✗ |
| `DOCKER_WORLD_NETWORK` | Réseau des mondes | `minehost-network` | ✗ |

**Note**: Ces variables sont rarement modifiées sauf en environnement personnalisé.

## Fichier .env complet avec commentaires

```bash
# ============================================
# Configuration MySQL
# ============================================
# Utilisateur de la base de données
MYSQL_DB_USERNAME=minehost

# Mot de passe IMPORTANT: À changer en production!
MYSQL_DB_PASSWORD=minehost_password_secure_change_me

# Nom de la base de données
MYSQL_DATABASE=minehost

# ============================================
# Configuration ngrok (optionnel)
# ============================================
# Laissez vide si vous n'utilisez pas ngrok
# Obtenez votre token sur https://dashboard.ngrok.com
NGROK_TOKEN=

# ============================================
# Configuration de la base de données
# ============================================
# Format: jdbc:mysql://host:port/database
# Ne modifiez que si vous changez l'architecture
DATABASE_URL=jdbc:mysql://mysql:3306/minehost

# ============================================
# Configuration Docker (optionnel - expert)
# ============================================
DOCKER_HOST=unix:///var/run/docker.sock
DOCKER_WORLD_IMAGE=minecraft-world:latest
DOCKER_WORLD_NETWORK=minehost-network
```

## Comment configurer

### Première utilisation

1. Copier le fichier d'exemple:
   ```bash
   cp .env.example .env
   ```

2. Éditer `.env` avec votre éditeur préféré:
   ```bash
   nano .env        # Linux/Mac
   notepad .env     # Windows
   ```

3. Modifier au minimum:
   - `MYSQL_DB_PASSWORD` (mot de passe sécurisé)
   - `NGROK_TOKEN` (optionnel, si vous avez un compte)

4. Sauvegarder et quitter

### Utilisation en production

Recommandations de sécurité:

```env
# 1. Mot de passe fort (min 16 caractères)
MYSQL_DB_PASSWORD=dR7#mK9$xQw2pLvN8bY@jT5&sF3aHcUe

# 2. Utilisateur différent
MYSQL_DB_USERNAME=prodadmin

# 3. Token ngrok sécurisé
NGROK_TOKEN=3_1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123

# 4. URL correcte pour l'environnement
DATABASE_URL=jdbc:mysql://mysql-db.prod:3306/minehost
```

## Dépannage des variables

### Erreur: "Cannot connect to database"
**Cause**: `DATABASE_URL` incorrect
**Vérification**:
```bash
# Vérifier la connectivité
docker exec minehost ping -c 3 mysql
```

### Erreur: "Cannot login"
**Cause**: `MYSQL_DB_USERNAME` ou `MYSQL_DB_PASSWORD` incorrect
**Solution**: Vérifier les espaces ou caractères spéciaux

### ngrok ne fonctionne pas
**Cause**: `NGROK_TOKEN` incorrect ou vide
**Solution**: 
- Laisser vide si vous n'en avez pas besoin
- Ou obtenir un token valide sur ngrok.com

### Port déjà utilisé
**Cause**: Services conflictants
**Solution**: Utiliser des ports différents dans le fichier docker-compose

## Fichier docker-compose.yml - Relation avec .env

Le fichier `docker-compose.yml` utilise les variables du `.env`:

```yaml
services:
  mysql:
    environment:
      MYSQL_DB_USERNAME: ${MYSQL_DB_USERNAME}           # ← Depuis .env
      MYSQL_DB_PASSWORD: ${MYSQL_DB_PASSWORD}           # ← Depuis .env

  minehost:
    environment:
      DATABASE_URL: ${DATABASE_URL}                     # ← Depuis .env
      NGROK_TOKEN: ${NGROK_TOKEN}                      # ← Depuis .env
```

## Points importants

⚠️ **Ne commitez jamais le fichier `.env`** avec des données sensibles:
```bash
# Ajouter à .gitignore (déjà fait normalement)
.env
```

📝 **Versionner uniquement `.env.example`**:
```bash
git add .env.example
git add .gitignore
git commit -m "Add environment example and ignore .env"
```

🔄 **Recharger après modification**:
```bash
# Redémarrer docker-compose pour appliquer les changements
docker compose up -d --force-recreate
```

## Variables personnalisées

Si vous avez besoin de variables supplémentaires:

1. Ajouter à `.env`:
   ```env
   MA_VARIABLE=valeur
   ```

2. Utiliser dans `docker-compose.yml`:
   ```yaml
   environment:
     - MA_VARIABLE=${MA_VARIABLE}
   ```

3. Relancer: `docker compose up -d`

## Ressources utiles

- [Documentation ngrok](https://ngrok.com/docs)
- [Documentation MySQL](https://dev.mysql.com/doc/)
- [Docker Compose et variables d'environnement](https://docs.docker.com/compose/environment-variables/)

---

**Besoin d'aide?** Vérifiez [DOCKER_SETUP.md](DOCKER_SETUP.md) ou [QUICKSTART.md](QUICKSTART.md)
