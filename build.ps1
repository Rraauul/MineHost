# Script de construction et déploiement de MineHost (Windows)

Write-Host "==========================================" -ForegroundColor Green
Write-Host "MineHost - Build & Deploy Script" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

# Vérifier si Docker est installé
$dockerCheck = docker --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Docker n'est pas installé" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Docker détecté: $dockerCheck" -ForegroundColor Green

# Aller dans le répertoire du projet
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $scriptPath

# Étape 1: Construire l'image minecraft-world
Write-Host ""
Write-Host "[1/3] Construction de l'image minecraft-world:latest..." -ForegroundColor Yellow
docker build -f backend/mondes/Dockerfile -t minecraft-world:latest backend/mondes/
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erreur lors de la construction de l'image" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Image minecraft-world:latest construite" -ForegroundColor Green

# Étape 2: Construire et démarrer les containers avec docker-compose
Write-Host ""
Write-Host "[2/3] Démarrage des services Docker Compose..." -ForegroundColor Yellow
docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erreur lors du démarrage des services" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Services Docker Compose démarrés" -ForegroundColor Green

# Étape 3: Vérifier que tout fonctionne
Write-Host ""
Write-Host "[3/3] Vérification de l'état des services..." -ForegroundColor Yellow
Write-Host ""
docker compose ps

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "✓ Installation complète!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services disponibles:"
Write-Host "  • API MineHost: http://localhost:8080"
Write-Host "  • MySQL: localhost:3308"
Write-Host ""
Write-Host "Pour voir les logs:"
Write-Host "  docker compose logs -f minehost"
Write-Host ""
Write-Host "Pour arrêter:"
Write-Host "  docker compose down"
Write-Host ""
