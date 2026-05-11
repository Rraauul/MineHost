#!/bin/bash

# Script d'entrée pour le container Docker du serveur Minecraft
# Ce script démarre le serveur et ngrok

set -e

WORLD_NAME="${WORLD_NAME:-world}"
SERVER_PORT="${SERVER_PORT:-25565}"
NGROK_TOKEN="${NGROK_TOKEN:-}"

echo "Démarrage du serveur Minecraft: $WORLD_NAME"
echo "Port: $SERVER_PORT"

# Véifier si le script de démarrage existe
if [ ! -f "/world/$WORLD_NAME.sh" ]; then
    echo "Erreur: Le script de démarrage /world/$WORLD_NAME.sh n'existe pas"
    exit 1
fi

# Rendre le script exécutable
chmod +x "/world/$WORLD_NAME.sh"

# Démarrer le serveur Minecraft en arrière-plan
echo "Lancement du serveur Minecraft..."
bash "/world/$WORLD_NAME.sh" &
SERVER_PID=$!

# Attendre que le serveur soit prêt
sleep 5

# Démarrer ngrok si un token est disponible
if [ -n "$NGROK_TOKEN" ]; then
    echo "Démarrage de ngrok avec le token fourni..."
    ngrok config add-authtoken "$NGROK_TOKEN"
    
    # Démarrer ngrok
    ngrok tcp $SERVER_PORT --log=stdout > /world/ngrok.log 2>&1 &
    NGROK_PID=$!
    
    # Attendre que ngrok soit prêt et récupérer l'URL
    sleep 3
    
    # Chercher l'URL d'exposition
    if [ -f "/world/ngrok.log" ]; then
        NGROK_URL=$(grep -oP "(?<=url=tcp://).+?(?= )" /world/ngrok.log | head -1)
        if [ -n "$NGROK_URL" ]; then
            echo "Serveur disponible à: tcp://$NGROK_URL"
            echo "tcp://$NGROK_URL" > /world/ngrok_url.txt
        fi
    fi
fi

# Garder le container actif
wait $SERVER_PID
