# Image de base
FROM ubuntu:22.04

# Installation des dépendances (Java 21 pour le backend Spring, Maven, et le CLI Docker
# qui sert au backend pour parler à /var/run/docker.sock monté depuis l'hôte).
RUN apt-get update && \
    apt-get install -y \
    openjdk-21-jdk \
    maven \
    curl \
    docker.io && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# NB: on garde l'utilisateur "fuentesr" pour la cohérence des paths (/home/fuentesr/mondes)
# mais on EXÉCUTE le backend en root. Raison : /var/run/docker.sock est exposé par
# l'hôte avec un GID "docker" qui n'a pas le même numéro à l'intérieur du container.
# Tant qu'on n'a pas de mécanisme dynamique pour aligner le GID (group_add côté compose
# ou entrypoint qui re-chown le socket), tourner en root est le plus simple. C'est OK
# pour une phase TT (test techno) — à durcir avant la prod.
RUN useradd -ms /bin/bash fuentesr && \
    mkdir -p /home/fuentesr/mondes && \
    chown -R fuentesr:fuentesr /home/fuentesr

WORKDIR /home/fuentesr

# Copie des sources du backend
COPY --chown=fuentesr:fuentesr ./backend/pom.xml .
COPY --chown=fuentesr:fuentesr ./backend/src ./src

# Templates "source" : conservés hors de /home/fuentesr/mondes (qui est recouvert
# par le volume hôte au runtime). DockerService / WorldService peuvent les utiliser
# pour amorcer le dossier hôte si besoin.
COPY --chown=fuentesr:fuentesr ./backend/mondes/Templates /opt/minehost/templates-source

# Contexte de build de l'image "monde" : copié dans /opt (PAS recouvert par le mount
# hôte), ce qui permet à DockerService.ensureWorldImage() de builder l'image
# `minehost/world:latest` au démarrage du backend en envoyant ce dossier comme
# contexte au daemon Docker.
COPY ./backend/mondes/Dockerfile     /opt/minehost/world-image/Dockerfile
COPY ./backend/mondes/entrypoint.sh  /opt/minehost/world-image/entrypoint.sh

# Compilation du JAR
RUN mvn clean package -DskipTests

# Port REST du backend Spring
EXPOSE 8080

# Les ports des MONDES ne sont PLUS exposés ici : chaque monde tourne dans son propre
# container et publie son propre port hôte (cf. DockerService.startWorldContainer).

# Point de montage pour les mondes Minecraft (mappé 1:1 avec l'hôte par docker-compose).
VOLUME ["/home/fuentesr/mondes"]

# Démarrage
CMD ["sh", "-c", "java -jar target/*.jar"]
