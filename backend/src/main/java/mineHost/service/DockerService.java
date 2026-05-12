package mineHost.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service responsable du cycle de vie des containers "monde Minecraft".
 *
 * Modèle :
 *   - Le backend Spring tourne dans le container `minehost`.
 *   - Chaque monde tourne dans son PROPRE container basé sur l'image
 *     `minehost/world:latest` que ce service construit au démarrage.
 *   - Le backend pilote tout via `/var/run/docker.sock` (monté depuis l'hôte).
 *
 * Conventions :
 *   - Nom de container : "minehost_world_<worldId>"
 *   - Port DANS le container : 25565 (fixe ; cohérent avec server.properties)
 *   - Port HÔTE : alloué dynamiquement par {@link #findFreeHostPort()} dans la
 *     plage [HOST_PORT_MIN, HOST_PORT_MAX]. C'est ce port que les joueurs utilisent
 *     pour se connecter, et c'est ce que WorldService stocke dans world.localPort.
 */
@Service
public class DockerService {

    private static final Logger log = LoggerFactory.getLogger(DockerService.class);

    private static final String WORLD_IMAGE = "minehost/world:latest";
    private static final String CONTAINER_WORLD_PATH = "/world";

    /**
     * Contexte de build de l'image "monde". Préparé par le backend/Dockerfile
     * (cf. les `COPY ./backend/mondes/...` vers /opt/minehost/world-image/).
     * On utilise ce chemin plutôt que /home/fuentesr/mondes/ parce que ce dernier
     * est recouvert par le volume hôte au runtime → les fichiers copiés à la
     * construction de l'image y sont invisibles.
     */
    private static final String WORLD_IMAGE_BUILD_CONTEXT = "/opt/minehost/world-image";

    /**
     * Plage de ports hôtes utilisable pour exposer les mondes.
     * On ne réserve PLUS cette plage côté minehost dans docker-compose.yml
     * (cf. commentaire du compose) afin que les containers monde puissent la
     * binder eux-mêmes.
     */
    private static final int HOST_PORT_MIN = 25565;
    private static final int HOST_PORT_MAX = 25700;

    private final DockerClient dockerClient;

    /**
     * Chemin de base des mondes SUR L'HÔTE (= mount 1:1 dans le backend).
     * Exemple : /home/fuentesr/mondes
     * Injecté depuis application.properties / variable d'environnement.
     */
    @Value("${app.worlds.host-base-path}")
    private String hostWorldsBasePath;

    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    // ------------------------------------------------------------------
    // Bootstrap : build l'image "monde" au démarrage si elle n'existe pas.
    // ------------------------------------------------------------------

    @PostConstruct
    public void ensureWorldImage() {
        try {
            InspectImageResponse img = dockerClient.inspectImageCmd(WORLD_IMAGE).exec();
            log.info("Image '{}' déjà présente (id={}), build sauté.", WORLD_IMAGE, img.getId());
            return;
        } catch (NotFoundException e) {
            log.info("Image '{}' absente, build en cours depuis {}…", WORLD_IMAGE, WORLD_IMAGE_BUILD_CONTEXT);
        } catch (Exception e) {
            log.warn("Inspect de '{}' a échoué ({}). On tente quand même le build.", WORLD_IMAGE, e.getMessage());
        }

        File contextDir = new File(WORLD_IMAGE_BUILD_CONTEXT);
        File dockerfile = new File(contextDir, "Dockerfile");
        if (!dockerfile.exists()) {
            log.error("Dockerfile introuvable à {} — l'image monde ne pourra pas être buildée. " +
                    "Vérifie le backend/Dockerfile (COPY vers /opt/minehost/world-image/).",
                    dockerfile.getAbsolutePath());
            return;
        }

        try {
            String imageId = dockerClient.buildImageCmd(dockerfile)
                    .withTags(Set.of(WORLD_IMAGE))
                    .withPull(false)
                    .withNoCache(false)
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();
            log.info("Image '{}' buildée avec succès (id={}).", WORLD_IMAGE, imageId);
        } catch (Exception e) {
            log.error("Échec du build de l'image '{}': {}", WORLD_IMAGE, e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Allocation de port hôte
    // ------------------------------------------------------------------

    /**
     * Renvoie l'ensemble des ports hôtes actuellement bindés par des containers
     * Docker (running ou non). Utilisé pour éviter les collisions au moment où
     * on crée un nouveau container monde.
     */
    public Set<Integer> getUsedHostPorts() {
        Set<Integer> used = new HashSet<>();
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container c : containers) {
            ContainerPort[] ports = c.getPorts();
            if (ports == null) continue;
            for (ContainerPort p : ports) {
                Integer pub = p.getPublicPort();
                if (pub != null) used.add(pub);
            }
        }
        return used;
    }

    /**
     * Trouve un port hôte libre dans la plage [HOST_PORT_MIN, HOST_PORT_MAX].
     * Si `preferred` est dans la plage et libre, on le réutilise (utile pour
     * conserver le même port d'un démarrage à l'autre).
     */
    public int findFreeHostPort(Integer preferred) {
        Set<Integer> used = getUsedHostPorts();
        if (preferred != null && preferred >= HOST_PORT_MIN && preferred <= HOST_PORT_MAX
                && !used.contains(preferred)) {
            return preferred;
        }
        for (int p = HOST_PORT_MIN; p <= HOST_PORT_MAX; p++) {
            if (!used.contains(p)) return p;
        }
        throw new RuntimeException("Aucun port libre dans [" + HOST_PORT_MIN + "," + HOST_PORT_MAX + "]");
    }

    // ------------------------------------------------------------------
    // Cycle de vie des containers monde
    // ------------------------------------------------------------------

    /**
     * Crée et démarre un container dédié à un monde.
     *
     * @param worldId     id du monde (sert à nommer le container)
     * @param userId      id du propriétaire (pour retrouver user_X)
     * @param worldName   nom du monde (= sous-dossier dans user_X)
     * @param worldScript nom du script à lancer dans /world (ex. "<name>.sh" ou "run.sh")
     * @param ramGo       RAM allouée en Go
     * @param hostPort    port exposé sur l'hôte
     * @return ID du container Docker créé
     */
    public String startWorldContainer(Integer worldId, Integer userId, String worldName,
                                      String worldScript, Integer ramGo, int hostPort) {

        String containerName = "minehost_world_" + worldId;

        // Path normalisé (évite "//user_1/..." si hostWorldsBasePath finit par "/").
        String base = hostWorldsBasePath.endsWith("/")
                ? hostWorldsBasePath.substring(0, hostWorldsBasePath.length() - 1)
                : hostWorldsBasePath;
        String hostWorldPath = base + "/user_" + userId + "/" + worldName;

        // Cleanup si un container du même nom traîne (état: exited/created/running).
        removeContainerIfExists(containerName);

        // Volume : dossier du monde sur l'hôte ↔ /world dans le container.
        Bind worldBind = new Bind(hostWorldPath, new Volume(CONTAINER_WORLD_PATH));

        // Port : 25565 (interne, canonique) → hostPort (hôte).
        ExposedPort minecraftPort = ExposedPort.tcp(25565);
        Ports portBindings = new Ports();
        portBindings.bind(minecraftPort, Ports.Binding.bindPort(hostPort));

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(worldBind)
                .withPortBindings(portBindings)
                .withMemory((long) ramGo * 1024L * 1024L * 1024L) // limite RAM en octets
                .withRestartPolicy(RestartPolicy.noRestart());

        CreateContainerResponse container = dockerClient.createContainerCmd(WORLD_IMAGE)
                .withName(containerName)
                .withHostConfig(hostConfig)
                .withExposedPorts(minecraftPort)
                // Variables d'environnement lues par entrypoint.sh :
                //   - WORLD_SCRIPT : nom du .sh à exécuter dans /world.
                .withEnv("WORLD_SCRIPT=" + (worldScript == null ? "" : worldScript))
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        log.info("Container '{}' démarré (id={}, hostPort={}, worldScript={})",
                containerName, container.getId(), hostPort, worldScript);
        return container.getId();
    }

    /**
     * Arrête le container d'un monde (sans le supprimer).
     */
    public void stopWorldContainer(Integer worldId) {
        String containerName = "minehost_world_" + worldId;
        try {
            dockerClient.stopContainerCmd(containerName)
                    .withTimeout(30) // 30s pour un arrêt propre du serveur MC
                    .exec();
            log.info("Container '{}' arrêté.", containerName);
        } catch (NotFoundException e) {
            log.info("stopWorldContainer: container '{}' déjà absent.", containerName);
        }
    }

    /**
     * Supprime le container d'un monde (à appeler après stop, ou pour cleanup).
     */
    public void removeWorldContainer(Integer worldId) {
        removeContainerIfExists("minehost_world_" + worldId);
    }

    /**
     * True si un container nommé "minehost_world_<id>" existe (peu importe son état).
     */
    public boolean worldContainerExists(Integer worldId) {
        String containerName = "minehost_world_" + worldId;
        try {
            dockerClient.inspectContainerCmd(containerName).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private void removeContainerIfExists(String containerName) {
        try {
            dockerClient.removeContainerCmd(containerName)
                    .withForce(true)
                    .exec();
            log.info("Ancien container '{}' supprimé.", containerName);
        } catch (NotFoundException e) {
            // Pas de container à supprimer, OK
        }
    }
}
