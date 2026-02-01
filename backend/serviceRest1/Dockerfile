# Image de base
FROM ubuntu:22.04

# Configuration de l'environnement
ENV DEBIAN_FRONTEND=noninteractive \
    NGROK_TOKEN=2vDdGqzFSPpojuMFbyP3cPswqJK_4s9VjoAHXFGt3AZNXCsXT
ENV DATABASE_URL=jdbc:mysql://mysql:3306/db_gestion_minecraft

# Installation des dépendances
RUN apt-get update && \
    apt-get install -y \
    openjdk-17-jdk \
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
    # Nettoyage
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Configuration de l'utilisateur et structure de base
RUN useradd -ms /bin/bash fuentesr && \
    mkdir -p /home/fuentesr/mondes && \
    chown -R fuentesr:fuentesr /home/fuentesr

# Passage à l'utilisateur fuentesr
USER fuentesr
WORKDIR /home/fuentesr

# Copie des fichiers
COPY --chown=fuentesr:fuentesr pom.xml .
COPY --chown=fuentesr:fuentesr src ./src
COPY --chown=fuentesr:fuentesr mondes/Templates/TemplateSimpleMC ./mondes/Templates/TemplateSimpleMC

# Construction de l'application
RUN mvn clean package

# Configuration ngrok
RUN ngrok config add-authtoken 2vDdGqzFSPpojuMFbyP3cPswqJK_4s9VjoAHXFGt3AZNXCsXT
# Exposition du port API
EXPOSE 8081

# Point de montage général pour les mondes
VOLUME ["/home/fuentesr/mondes"]

# Commande de démarrage simplifiée
CMD ["java", "-jar", "/home/fuentesr/target/restApi1-0.0.1-SNAPSHOT.jar"]