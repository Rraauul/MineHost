package mineHost.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DockerService {

    private final DockerClient dockerClient;
    
    @Value("${docker.world.image:minecraft-world:latest}")
    private String worldImageName;
    
    @Value("${docker.world.network:minehost-network}")
    private String worldNetwork;
    
    @Value("${docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;

    public DockerService() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        this.dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    /**
     * Crée et démarre un container Docker pour un monde Minecraft
     * 
     * @param worldId ID du monde
     * @param worldName Nom du monde
     * @param worldPath Chemin absolu du monde sur l'hôte
     * @param port Port local pour le serveur
     * @return ID du container créé
     */
    public String startWorldContainer(Integer worldId, String worldName, String worldPath, int port) {
        String containerName = "world_" + worldId;

        try {
            // Création du container avec les configurations nécessaires
            String containerId = dockerClient.createContainerCmd(worldImageName)
                    .withName(containerName)
                    .withHostConfig(
                            HostConfig.newHostConfig()
                                    .withBinds(worldPath + ":/world")
                                    .withPortBindings(
                                            new Ports().bind(Ports.Binding.bindPort(port), new Ports.Port("tcp", 25565)),
                                            new Ports().bind(Ports.Binding.bindPort(port + 1), new Ports.Port("tcp", port + 1))
                                    )
                                    .withNetworkMode(worldNetwork)
                    )
                    .withVolumes(new Volume("/world"))
                    .withEnv("WORLD_NAME=" + worldName, "SERVER_PORT=" + port, 
                            "NGROK_TOKEN=${NGROK_TOKEN}")
                    .exec()
                    .getId();

            // Démarrage du container
            dockerClient.startContainerCmd(containerId).exec();

            return containerId;
        } catch (DockerException e) {
            throw new RuntimeException("Erreur lors de la création du container Docker: " + e.getMessage(), e);
        }
    }

    /**
     * Arrête et supprime un container Docker
     * 
     * @param worldId ID du monde
     */
    public void stopWorldContainer(Integer worldId) {
        String containerName = "world_" + worldId;
        try {
            dockerClient.stopContainerCmd(containerName).exec();
            dockerClient.removeContainerCmd(containerName).exec();
        } catch (DockerException e) {
            throw new RuntimeException("Erreur lors de l'arrêt du container: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie si un container est en cours d'exécution
     * 
     * @param worldId ID du monde
     * @return true si le container est en cours d'exécution
     */
    public boolean isContainerRunning(Integer worldId) {
        String containerName = "world_" + worldId;
        try {
            List<com.github.dockerjava.api.model.Container> containers = dockerClient
                    .listContainersCmd()
                    .exec();
            return containers.stream()
                    .anyMatch(c -> c.getNames()[0].equals("/" + containerName));
        } catch (DockerException e) {
            return false;
        }
    }

    /**
     * Obtient les logs du container
     * 
     * @param worldId ID du monde
     * @return Contenu des logs
     */
    public String getContainerLogs(Integer worldId) {
        String containerName = "world_" + worldId;
        try {
            return dockerClient.logContainerCmd(containerName)
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(new com.github.dockerjava.core.command.LogContainerResultCallback())
                    .awaitCompletion()
                    .toString();
        } catch (DockerException | InterruptedException e) {
            throw new RuntimeException("Erreur lors de la récupération des logs: " + e.getMessage(), e);
        }
    }
}
