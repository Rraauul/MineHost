# Image de base
FROM ubuntu:22.04

# Installation des dépendances (Java 21 et Maven)
RUN apt-get update && \
    apt-get install -y \
    openjdk-21-jdk \
    maven \
    screen \
    curl && \
    # Installation de ngrok
    curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc | \
    tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && \
    echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | \
    tee /etc/apt/sources.list.d/ngrok.list && \
    apt-get update && \
    apt-get install -y ngrok && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Configuration de l'utilisateur
RUN useradd -ms /bin/bash fuentesr && \
    mkdir -p /home/fuentesr/mondes && \
    chown -R fuentesr:fuentesr /home/fuentesr

USER fuentesr
WORKDIR /home/fuentesr

# Copie des sources
COPY --chown=fuentesr:fuentesr ./backend/pom.xml .
COPY --chown=fuentesr:fuentesr ./backend/src ./src
COPY --chown=fuentesr:fuentesr ./backend/mondes/Templates/TemplateSimpleMC ./mondes/Templates/TemplateSimpleMC

# Compilation du projet unique (Génère le JAR dans target/)
RUN mvn clean package -DskipTests

# Exposition du port (8080 pour la Gateway combinée)
EXPOSE 8080

# Point de montage pour les mondes Minecraft
VOLUME ["/home/fuentesr/mondes"]

# COMMANDE DE DÉMARRAGE CORRIGÉE
# Utilisez une étoile (*) pour éviter les erreurs de nom de fichier versionné
CMD ["sh", "-c", "java -jar target/*.jar"]