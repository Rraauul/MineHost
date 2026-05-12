#!/bin/bash
# Entrypoint du container "monde Minecraft".
# Stratégie :
#   1. On se place dans /world (monté depuis l'hôte).
#   2. On exécute, dans l'ordre de préférence :
#        a) le script nommé via la variable d'environnement WORLD_SCRIPT (transmise
#           par le backend Spring depuis DockerService),
#        b) à défaut, run.sh (lanceur "simple" généré par ServerPackCreator
#           pour Forge : il appelle directement `java @user_jvm_args.txt
#           @libraries/.../unix_args.txt nogui`),
#        c) à défaut, on échoue clairement.
# On évite délibérément `start.sh` qui est interactif (read clavier, install
# de Java, etc.) et fait planter le container quand il n'y a pas de TTY.

set -e
cd /world

run_script() {
    local script="$1"
    echo "[entrypoint] Lancement de ${script}"
    chmod +x "${script}"
    exec bash "${script}" nogui
}

if [ -n "${WORLD_SCRIPT}" ] && [ -f "${WORLD_SCRIPT}" ]; then
    run_script "${WORLD_SCRIPT}"
fi

if [ -f "run.sh" ]; then
    run_script "run.sh"
fi

echo "[entrypoint] Aucun script de démarrage trouvé (ni \$WORLD_SCRIPT='${WORLD_SCRIPT}' ni run.sh)." >&2
ls -la /world >&2
exit 1
