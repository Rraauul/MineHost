# Dockerfile pour exécuter un serveur Minecraft
# Ce dockerfile sera utilisé pour créer des images pour chaque monde

FROM ubuntu:22.04

# Installation des dépendances
RUN apt-get update && \
    apt-get install -y \
    openjdk-21-jdk \
    curl \
    bash && \
    # Installation de ngrok
    curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc | \
    tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && \
    echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | \
    tee /etc/apt/sources.list.d/ngrok.list && \
    apt-get update && \
    apt-get install -y ngrok && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Création de l'utilisateur et du répertoire
RUN useradd -ms /bin/bash minecraft && \
    mkdir -p /world && \
    chown -R minecraft:minecraft /world

USER minecraft
WORKDIR /world

# Exposition des ports (ajustables à la création du container)
EXPOSE 25565

# Script de démarrage
COPY --chown=minecraft:minecraft entrypoint.sh /world/entrypoint.sh
RUN chmod +x /world/entrypoint.sh

# Point de montage pour le monde
VOLUME ["/world"]

# Démarrage du serveur et ngrok
ENTRYPOINT ["/world/entrypoint.sh"]
