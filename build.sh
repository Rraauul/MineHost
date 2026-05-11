#!/bin/bash

# Script de construction et déploiement de MineHost

set -e

echo "=========================================="
echo "MineHost - Build & Deploy Script"
echo "=========================================="

# Couleurs pour les messages
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Vérifier si Docker est installé
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker n'est pas installé${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker détecté${NC}"

# Aller dans le répertoire du projet
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Étape 1: Construire l'image minecraft-world
echo ""
echo -e "${YELLOW}[1/3] Construction de l'image minecraft-world:latest...${NC}"
docker build -f backend/mondes/Dockerfile -t minecraft-world:latest backend/mondes/
echo -e "${GREEN}✓ Image minecraft-world:latest construite${NC}"

# Étape 2: Construire et démarrer les containers avec docker-compose
echo ""
echo -e "${YELLOW}[2/3] Démarrage des services Docker Compose...${NC}"
docker compose up -d --build
echo -e "${GREEN}✓ Services Docker Compose démarrés${NC}"

# Étape 3: Vérifier que tout fonctionne
echo ""
echo -e "${YELLOW}[3/3] Vérification de l'état des services...${NC}"
echo ""
docker compose ps

echo ""
echo -e "${GREEN}=========================================="
echo "✓ Installation complète!"
echo "=========================================${NC}"
echo ""
echo "Services disponibles:"
echo "  • API MineHost: http://localhost:8080"
echo "  • MySQL: localhost:3308"
echo ""
echo "Pour voir les logs:"
echo "  docker compose logs -f minehost"
echo ""
echo "Pour arrêter:"
echo "  docker compose down"
echo ""
