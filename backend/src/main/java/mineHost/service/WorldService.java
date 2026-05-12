package mineHost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mineHost.dto.WorldDTO;
import mineHost.model.Server;
import mineHost.model.World;
import mineHost.repository.ServerRepository;
import mineHost.repository.WorldRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class WorldService {

    private final ServerRepository serverRepository;

    private final WorldRepository worldRepository;

    private final DockerService dockerService;

    @Value("${app.worlds.host-base-path}")
    private String basePath;

    @Autowired
    public WorldService(WorldRepository worldRepository, ServerRepository serverRepository,
            DockerService dockerService) {
        this.worldRepository = worldRepository;
        this.serverRepository = serverRepository;
        this.dockerService = dockerService;
    }

    @Transactional
    public ResponseEntity<World> createWorld(String name, String template, Integer ram, Integer userId,
            Integer serverId) {
        Server server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 - Serveur non trouvé
        }

        File userDir = new File(basePath + "user_" + userId);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        File templateDir = new File(basePath + "Templates/" + template);
        File newWorldDir = new File(userDir, name);

        if (newWorldDir.exists()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 - Conflit : Monde déjà existant
        }

        if (!templateDir.exists() || !templateDir.isDirectory()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 - Modèle non trouvé
        }

        try {
            copyDirectory(templateDir, newWorldDir);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 - Erreur interne
        }

        File startScript = new File(newWorldDir, "start.sh");
        File renamedScript = new File(newWorldDir, name + ".sh");
        if (startScript.exists()) {
            try {
                Files.move(startScript.toPath(), renamedScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 - Erreur interne
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 - start.sh manquant
        }

        File variabFile = new File(newWorldDir, "variables.txt");
        if (variabFile.exists()) {
            try {
                String content = Files.readString(variabFile.toPath());
                content = content.replaceAll("JAVA_ARGS=\"-Xmx\\d+G -Xms\\d+G\"",
                        "JAVA_ARGS=\"-Xmx" + ram + "G -Xms" + ram + "G\"");
                Files.writeString(variabFile.toPath(), content);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();// 500 - Erreur interne
            }
        }

        File userJvmArgs = new File(newWorldDir, "user_jvm_args.txt");
        if (userJvmArgs.exists()) {
            try {
                String content = Files.readString(userJvmArgs.toPath());
                content = content.replaceAll("-Xmx\\d+G -Xms\\d+G", "-Xmx" + ram + "G -Xms" + ram + "G");
                Files.writeString(userJvmArgs.toPath(), content);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 - Erreur interne
            }
        }

        World newWorld = new World();

        newWorld.setName(name);
        newWorld.setTemplate(template);
        newWorld.setRam(ram);
        newWorld.setSize(getDirectorySize(newWorldDir) / (1024 * 1024 * 1024)); // Convertir en Go
        newWorld.setStatus("Stopped");
        newWorld.setDateCreation(java.time.LocalDateTime.now());
        newWorld.setLocalPort(25565); // Trouver un port disponible
        newWorld.setFkUser(userId);
        newWorld.setFkServer(server);
        worldRepository.save(newWorld);

        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 - Monde créé
    }

    /**
     * Force server.properties à utiliser le port "canonique" 25565 *à l'intérieur*
     * du container monde. Le mapping vers le port hôte (visible des joueurs) est
     * géré par Docker (cf. DockerService.startWorldContainer).
     *
     * Bug historique corrigé : l'ancienne version faisait
     *   "rcon.port=" + newPort + 1
     * qui en Java fait une CONCATÉNATION de strings ("25565" + "1" = "255651"),
     * pas une addition. On force désormais 25575 (valeur par défaut MC).
     */
    private void normalizeServerPropertiesForContainer(File worldDir) {
        File serverPropertiesFile = new File(worldDir, "server.properties");
        if (!serverPropertiesFile.exists())
            return;
        try {
            String content = Files.readString(serverPropertiesFile.toPath());
            content = content.replaceAll("server-port=\\d+", "server-port=25565");
            content = content.replaceAll("query\\.port=\\d+", "query.port=25565");
            content = content.replaceAll("rcon\\.port=\\d+", "rcon.port=25575");
            Files.writeString(serverPropertiesFile.toPath(), content);
        } catch (IOException e) {
            throw new RuntimeException("Erreur écriture server.properties", e);
        }
    }

    /**
     * Décide quel script lancer DANS le container monde.
     * Priorité :
     *   1. {worldName}.sh (résultat du renommage de start.sh dans createWorld)
     *      — utile si tu veux garder le bootstrap ServerPackCreator un jour,
     *   2. run.sh (lanceur "simple" Forge — préféré : non interactif, démarre
     *      `java @user_jvm_args.txt @libraries/.../unix_args.txt nogui`).
     *
     * L'entrypoint du container honore la variable d'environnement WORLD_SCRIPT
     * en priorité, et tombe sur run.sh sinon. Ici on transmet `run.sh` parce
     * que c'est ce qui marche le mieux en container (pas de pause clavier, pas
     * de tentative d'installer Java au runtime).
     */
    private String pickWorldScript(File worldDir, String worldName) {
        File runSh = new File(worldDir, "run.sh");
        if (runSh.exists()) return "run.sh";
        File named = new File(worldDir, worldName + ".sh");
        if (named.exists()) return worldName + ".sh";
        return null; // l'entrypoint listera et échouera proprement
    }

    @Transactional
    public ResponseEntity<World> startWorldTT(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        World world = worldOptional.get();
        File worldDir = new File(basePath + "user_" + world.getFkUser() + "/" + world.getName());
        if (!worldDir.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 1) Allocation de port HÔTE via l'API Docker.
        //    On préfère réutiliser world.localPort s'il est encore libre, sinon
        //    on en trouve un autre dans la plage [25565, 25700].
        //    NB: l'ancien isPortInUse() ne marchait pas pour des ports bindés
        //    par d'AUTRES containers sur l'hôte (il testait depuis le namespace
        //    réseau du backend, qui n'a pas connaissance des ports hôte).
        int hostPort;
        try {
            hostPort = dockerService.findFreeHostPort((int) world.getLocalPort());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        world.setLocalPort(hostPort);

        // 2) On force server.properties à 25565 (port canonique DANS le container).
        //    Docker mappe ensuite 25565 (container) → hostPort (hôte).
        try {
            normalizeServerPropertiesForContainer(worldDir);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 3) Choix du script de démarrage à exécuter dans le container.
        String worldScript = pickWorldScript(worldDir, world.getName());

        try {
            dockerService.startWorldContainer(
                    world.getId(),
                    world.getFkUser(),
                    world.getName(),
                    worldScript,
                    world.getRam(),
                    hostPort);

            world.setStatus("Running");
            worldRepository.save(world);

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Version TT (test techno) de stopWorld : arrête le CONTAINER au lieu des
     * sessions `screen` (qui n'existent plus pour un monde lancé via TT).
     */
    @Transactional
    public ResponseEntity<World> stopWorldTT(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        World world = worldOptional.get();
        try {
            dockerService.stopWorldContainer(worldId);
            // On retire le container pour libérer le nom et le port hôte.
            dockerService.removeWorldContainer(worldId);
            world.setStatus("Stopped");
            worldRepository.save(world);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Version TT de deleteWorld : s'assure que le container est bien arrêté/supprimé
     * avant de supprimer les fichiers du monde.
     */
    @Transactional
    public ResponseEntity<World> deleteWorldTT(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        World world = worldOptional.get();

        // Cleanup du container si présent — pas grave s'il n'existe pas.
        try {
            if (dockerService.worldContainerExists(worldId)) {
                dockerService.stopWorldContainer(worldId);
                dockerService.removeWorldContainer(worldId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        File worldDir = new File(basePath + "user_" + world.getFkUser() + "/" + world.getName());
        if (worldDir.exists()) {
            try {
                deleteDirectory(worldDir);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        worldRepository.deleteById(worldId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Transactional
    public ResponseEntity<World> startWorld(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isPresent()) {
            World world = worldOptional.get();
            File worldDir = new File(basePath + "user_" + world.getFkUser() + "/" + world.getName());
            if (worldDir.exists()) {
                int port = (int) world.getLocalPort();

                // Vérifier si le port est déjà utilisé
                if (isPortInUse(port)) {
                    // Générer un nouveau port
                    int newPort = findAvailablePort();
                    world.setLocalPort(newPort);

                    // Mettre à jour le fichier server.properties avec le nouveau port
                    File serverPropertiesFile = new File(worldDir, "server.properties");
                    if (serverPropertiesFile.exists()) {
                        try {
                            String content = Files.readString(serverPropertiesFile.toPath());
                            content = content.replaceAll("server-port=\\d+", "server-port=" + newPort);
                            content = content.replaceAll("query.port=\\d+", "query.port=" + newPort);
                            content = content.replaceAll("rcon.port=\\d+", "rcon.port=" + newPort + 1);

                            Files.writeString(serverPropertiesFile.toPath(), content);
                        } catch (IOException e) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        }
                    }

                    // Sauvegarder le nouveau port dans la base de données
                    worldRepository.save(world);
                }

                try {
                    // Set executable permissions for the script
                    File scriptFile = new File(worldDir, world.getName() + ".sh");
                    ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x",
                            scriptFile.getAbsolutePath());
                    chmodProcessBuilder.start();
                    // Start the world using screen
                    ProcessBuilder processBuilder = new ProcessBuilder("screen", "-dmS", "world_" + worldId + "sh",
                            "bash", scriptFile.getAbsolutePath());
                    processBuilder.directory(worldDir);
                    processBuilder.start();

                    File ngrokFile = new File(worldDir, "ngrok");
                    ProcessBuilder chmodProcessNgrok = new ProcessBuilder("chmod", "+x",
                            ngrokFile.getAbsolutePath());
                    chmodProcessNgrok.start();
                    // Start ngrok using screen
                    ProcessBuilder processNgrok = new ProcessBuilder("screen", "-dmS", "world_" + worldId + "ng",
                            "bash", "-c", "./ngrok tcp " + world.getLocalPort() + " --log=stdout > ngrok.log 2>&1");

                    processNgrok.directory(worldDir);
                    processNgrok.start();
                    Thread.sleep(3000);

                    // Lire le fichier de log
                    Path logPath = Paths.get(worldDir.getAbsolutePath(), "ngrok.log");
                    List<String> lines = Files.readAllLines(logPath);

                    // Trouver la ligne contenant l'URL
                    for (String line : lines) {
                        if (line.contains("started tunnel")) {
                            String url = line.split("url=tcp://")[1].split(" ")[0];
                            world.setAddressNgrok(url);
                            break;
                        }
                    }

                    world.setStatus("Running");
                    worldRepository.save(world);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).build(); // 202 - Monde démarré
                } catch (IOException | InterruptedException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 - Erreur interne
                                                                                            // starting the world
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 - Répertoire du monde non trouvé
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 - Monde non trouvé
        }
    }

    @Transactional
    public ResponseEntity<World> stopWorld(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isPresent()) {
            World world = worldOptional.get();
            String screenNameSh = "world_" + worldId + "sh";
            String screenNameNg = "world_" + worldId + "ng";

            try {
                new ProcessBuilder("screen", "-S", screenNameSh, "-X", "stuff", "$'\003'").start();
                Thread.sleep(2000); // Attente courte
                new ProcessBuilder("screen", "-S", screenNameSh, "-X", "stuff", "$'\003'").start();
                new ProcessBuilder("screen", "-XS", screenNameSh, "quit").start();
                world.setStatus("Stopped");
                new ProcessBuilder("screen", "-S", screenNameNg, "-X", "stuff", "$'\003'").start();
                Thread.sleep(2000); // Attente courte
                new ProcessBuilder("screen", "-XS", screenNameNg, "quit").start();
                world.setStatus("Stopped");
                worldRepository.save(world);
            } catch (IOException | InterruptedException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 - Erreur interne
                                                                                        // arrêt du monde
            }

            return ResponseEntity.status(HttpStatus.OK).build(); // 200 - Monde arrêté
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 - Monde non trouvé
        }
    }

    @Transactional
    public ResponseEntity<World> deleteWorld(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isPresent()) {
            World world = worldOptional.get();
            if (world.getStatus().equals("Running")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 - Conflit : Monde en cours d'exécution

            }
            File worldDir = new File(basePath + "user_" + world.getFkUser() + "/" + world.getName());
            if (worldDir.exists()) {
                try {
                    deleteDirectory(worldDir);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 - Erreur interne
                                                                                            // suppression du monde
                }
            }
            worldRepository.deleteById(worldId);
            return ResponseEntity.status(HttpStatus.OK).build(); // 200 - Monde supprimé
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 - Monde non trouvé
        }
    }

    @Transactional
    public File downloadLog(Integer worldId) {
        Optional<World> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isPresent()) {
            World world = worldOptional.get();
            File worldDir = new File(basePath + "user_" + world.getFkUser() + "/" + world.getName());
            if (worldDir.exists()) {
                File logFile = new File(worldDir, "logs/latest.log");
                if (logFile.exists()) {
                    return logFile;
                }
            }
        }
        return null;
    }

    // Méthode pour supprimer un dossier et son contenu
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                deleteDirectory(file);
            }
        }
        Files.delete(directory.toPath());
    }

    // Méthode pour copier un dossier et son contenu
    private void copyDirectory(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        for (File file : source.listFiles()) {
            // Ignorer les fichiers contenant ":Zone.Identifier" dans leur nom
            // car selon windows mon serveur n'est pas d'une source de confiance
            if (file.getName().contains(":Zone.Identifier")) {
                continue;
            }
            File destFile = new File(destination, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, destFile);
            } else {
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    // Méthode pour avoir la taille d'un dossier en octets
    private Float getDirectorySize(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return Float.valueOf(0);
        }

        float size = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                size += file.length(); // Taille du fichier
            } else if (file.isDirectory()) {
                size += getDirectorySize(file); // Taille du sous-dossier
            }
        }
        return size;
    }

    public WorldDTO getWorldInfo(Integer worldId) {
        World world = worldRepository.findById(worldId).orElse(null);
        if (world == null) {
            return null;
        }

        // Mapper les données de l'entité World vers le DTO
        return new WorldDTO(
                world.getId(),
                world.getName(),
                world.getStatus(),
                world.getRam(),
                world.getSize(),
                world.getTemplate(),
                world.getDateCreation(),
                world.getAddressNgrok());
    }

    private int findAvailablePort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Unable to find an available port", e);
        }
    }

    private boolean isPortInUse(int port) {
        try (java.net.Socket socket = new java.net.Socket("localhost", port)) {
            return true; // Le port est utilisé
        } catch (IOException e) {
            return false; // Le port est libre
        }
    }

    public List<WorldDTO> getWorldsByUserId(Integer userId) {
        List<World> worlds = worldRepository.findByFkUser(userId);
        return worlds.stream().map(world -> new WorldDTO(
                world.getId(),
                world.getName(),
                world.getStatus(),
                world.getRam(),
                world.getSize(),
                world.getTemplate(),
                world.getDateCreation())).toList();
    }

    public List<String> getTemplates() {
        File templatesDir = new File(basePath + "Templates/");
        if (!templatesDir.exists() || !templatesDir.isDirectory()) {
            return List.of(); // Retourner une liste vide si le répertoire n'existe pas
        }

        return List.of(templatesDir.listFiles()).stream()
                .filter(File::isDirectory)
                .map(File::getName)
                .toList();
    }

}
