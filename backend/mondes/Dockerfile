# Image utilisée pour CHAQUE monde Minecraft (un container par monde).
# Construite UNE FOIS au démarrage du backend par DockerService.ensureWorldImage(),
# puis instanciée à chaque startWorld.
#
# Java 17 : c'est la version recommandée par les variables.txt du template
# (RECOMMENDED_JAVA_VERSION=17 pour Minecraft 1.20.1 + Forge 47.4.0).
FROM eclipse-temurin:17-jre

# Paquets minimaux : bash pour run.sh, curl/unzip pour les scripts qui pourraient
# vouloir télécharger des libs au runtime, procps pour debug.
RUN apt-get update && \
    apt-get install -y --no-install-recommends bash curl unzip procps ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# /world est monté au runtime depuis l'hôte (le dossier du monde de l'utilisateur).
WORKDIR /world

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Port "canonique" Minecraft DANS le container. Docker se charge de le remapper
# vers un port hôte choisi par DockerService.
EXPOSE 25565

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
