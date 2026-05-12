package mineHost.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mineHost.dto.AbonnementDTO;
import mineHost.dto.LoginRequestDTO;
import mineHost.dto.ServerDTO;
import mineHost.dto.UserDTO;
import mineHost.dto.WorldDTO;
import mineHost.model.Abonnement;
import mineHost.model.World;
import mineHost.service.AbonnementService;
import mineHost.service.DockerService;
import mineHost.service.ServerService;
import mineHost.service.UtilisateurService;
import mineHost.service.WorldService;

//@CrossOrigin(origins = { "https://fuentesr.emf-informatique.ch" }, allowCredentials = "true")
@CrossOrigin(origins = "${FRONTEND_URL}", allowCredentials = "true", allowedHeaders = "*")
@RestController
public class Controller {
    private final WorldService worldService;
    private final UtilisateurService utilisateurService;
    private final AbonnementService abonnementService;
    private final ServerService serverService;
    private final DockerService dockerService;

    @Autowired
    public Controller(WorldService worldService, UtilisateurService utilisateurService,
            AbonnementService abonnementService, ServerService serverService, DockerService dockerService) {
        this.worldService = worldService;
        this.utilisateurService = utilisateurService;
        this.abonnementService = abonnementService;
        this.serverService = serverService;
        this.dockerService = dockerService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(HttpSession session,
            @RequestBody LoginRequestDTO loginRequest) {
        boolean loginSuccess = utilisateurService.login(loginRequest.getName(), loginRequest.getMotDePasse());
        Map<String, String> response = new HashMap<>();

        if (loginSuccess) {
            session.setAttribute("user", loginRequest.getName());

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(loginRequest.getName());
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                session.setAttribute("pkUser", userResponse.getBody().getId());
            }

            response.put("message", "Connexion réussie");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response.put("message", "Mot de passe ou utilisateur incorrect (CTRL)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session, HttpServletResponse response) {
        // Récupérer le nom d'utilisateur avant de détruire la session
        String username = (String) session.getAttribute("user");

        // Vérifier si l'utilisateur est bien connecté
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Aucun utilisateur connecté");
        }

        // Supprimer l'attribut de session
        session.removeAttribute("user");

        // Invalider la session
        session.invalidate();

        // Créer un cookie avec la même clé mais valide 0 seconde pour effacer le cookie
        // existant
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0); // Cookie expiré immédiatement
        cookie.setPath("/");
        response.addCookie(cookie);

        // Journaliser la déconnexion (facultatif)
        System.out.println("Déconnexion de l'utilisateur: " + username);

        // Retourner une réponse positive
        return ResponseEntity.ok("Déconnexion réussie");
    }

    @PostMapping("/createWorld")
    public ResponseEntity<?> createWorld(HttpSession session,
            @RequestParam String name,
            @RequestParam String template,
            @RequestParam Integer ram,
            @RequestParam Integer serverId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            Integer userId = userResponse.getBody().getId();
            return worldService.createWorld(name, template, ram, userId, serverId);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @PostMapping("/startWorld")
    public ResponseEntity<?> startWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            Integer userId = userResponse.getBody().getId();
            List<WorldDTO> worldList = worldService.getWorldsByUserId(userId);

            // Vérifier si le monde appartient à l'utilisateur
            boolean isOwner = worldList.stream().anyMatch(w -> w.getId().equals(worldId));
            if (isOwner) {
                return worldService.startWorld(worldId);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce n'est pas ton monde");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @PostMapping("/createWorldWithoutSession")
    public ResponseEntity<?> createWorld(@RequestParam String name,
            @RequestParam String template,
            @RequestParam Integer ram,
            @RequestParam Integer serverId) {
        // Note: This method does not use the session attribute
        // You might want to add authentication logic here if needed
        return worldService.createWorld(name, template, ram, null, serverId);
    }

    @PostMapping("/startWorldWithoutSession")
    public ResponseEntity<?> startWorld(@RequestParam Integer worldId) {
        // Note: This method does not use the session attribute
        // You might want to add authentication logic here if needed
        return worldService.startWorldTT(worldId);
    }

    // ---- Endpoints "TT" (test techno : un container Docker par monde) -------
    // Ils existent en parallèle des endpoints classiques (qui utilisent encore
    // screen + ngrok). Quand le TT sera validé, on pourra basculer /stopWorld
    // et /deleteWorld dessus.

    @PostMapping("/stopWorldWithoutSession")
    public ResponseEntity<?> stopWorldTT(@RequestParam Integer worldId) {
        return worldService.stopWorldTT(worldId);
    }

    @DeleteMapping("/deleteWorldWithoutSession")
    public ResponseEntity<?> deleteWorldTT(@RequestParam Integer worldId) {
        return worldService.deleteWorldTT(worldId);
    }

    @GetMapping("/getWorldsByUserId")
    public ResponseEntity<?> getWorldsByUser(HttpSession session) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            Integer userId = userResponse.getBody().getId();
            List<WorldDTO> worlds = worldService.getWorldsByUserId(userId);
            return ResponseEntity.ok(worlds);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @PostMapping("/stopWorld")
    public ResponseEntity<?> stopWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            Integer userId = userResponse.getBody().getId();
            List<WorldDTO> worldList = worldService.getWorldsByUserId(userId);

            // Vérifier si le monde appartient à l'utilisateur
            boolean isOwner = worldList.stream().anyMatch(w -> w.getId().equals(worldId));
            if (isOwner) {
                return worldService.stopWorld(worldId);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce n'est pas ton monde");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @DeleteMapping("/deleteWorld")
    public ResponseEntity<?> deleteWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            Integer userId = userResponse.getBody().getId();
            List<WorldDTO> worldList = worldService.getWorldsByUserId(userId);

            // Vérifier si le monde appartient à l'utilisateur
            boolean isOwner = worldList.stream().anyMatch(w -> w.getId().equals(worldId));
            if (isOwner) {
                return worldService.deleteWorld(worldId);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce n'est pas ton monde");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @GetMapping("/infoWorld")
    public ResponseEntity<?> getInfoWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
            }

            Integer userId = userResponse.getBody().getId();
            List<WorldDTO> worldList = worldService.getWorldsByUserId(userId);

            // Vérifier si le monde appartient à l'utilisateur
            boolean isOwner = worldList.stream().anyMatch(w -> w.getId().equals(worldId));
            if (isOwner) {
                WorldDTO worldInfo = worldService.getWorldInfo(worldId);
                if (worldInfo == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Monde introuvable");
                }
                return ResponseEntity.ok(worldInfo);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ce n'est pas ton monde");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @GetMapping("/downloadLog")
    public ResponseEntity<Resource> downloadLog(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Integer userId = userResponse.getBody().getId();
            List<WorldDTO> worldList = worldService.getWorldsByUserId(userId);

            // Vérifier si le monde appartient à l'utilisateur
            boolean isOwner = worldList.stream().anyMatch(w -> w.getId().equals(worldId));
            if (isOwner) {
                File logFile = worldService.downloadLog(worldId);
                if (logFile == null || !logFile.exists()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                Resource fileResource = new FileSystemResource(logFile);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + logFile.getName())
                        .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                        .contentLength(logFile.length())
                        .body(fileResource);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/infoServer")
    public ResponseEntity<?> getServerInfo(@RequestParam Integer serverId) {
        ServerDTO serverInfo = serverService.getServerdInfo(serverId);
        if (serverInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Serveur introuvable");
        }
        return ResponseEntity.ok(serverInfo);
    }

    @GetMapping("/getTemplates")
    public ResponseEntity<?> getTemplates() {
        List<String> templates = worldService.getTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/infoOffers")
    public ResponseEntity<?> getInfoOffers() {
        List<Abonnement> offers = abonnementService.infoOffers();
        if (offers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/offerByClient")
    public ResponseEntity<?> getOfferByClient(HttpSession session) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");

            // Récupérer l'ID utilisateur
            ResponseEntity<UserDTO> userResponse = utilisateurService.getUserByUsername(username);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }

            Integer pkUser = userResponse.getBody().getId();
            return utilisateurService.getOffre(pkUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<String> addUser(@RequestParam String nom,
            @RequestParam String motDePasse,
            @RequestParam(required = false) Integer FK_abo) {
        return utilisateurService.addUser(nom, 0, motDePasse, FK_abo);
    }

    @PostMapping("/addEmeraude")
    public ResponseEntity<String> addEmeraude(@RequestParam Integer pkUser,
            @RequestParam Integer emeraudes) {
        return utilisateurService.addEmeraude(pkUser, emeraudes);
    }

    @PostMapping("/acheterAbo")
    public ResponseEntity<String> acheterAbo(HttpSession session, @RequestParam Integer pkUser,
            @RequestParam Integer Fkabonnement) {
        if (session.getAttribute("pkUser") == pkUser) {
            return utilisateurService.acheterAbonnement(pkUser, Fkabonnement);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        List<UserDTO> users = utilisateurService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/changeName")
    public ResponseEntity<String> changeName(@RequestParam Integer PK,
            @RequestParam String newName) {
        return utilisateurService.changeName(PK, newName);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestParam Integer PK,
            @RequestParam String password) {
        return utilisateurService.changePassword(PK, password);
    }

    @PostMapping("/addAbo")
    public ResponseEntity<String> addAbo(@RequestParam String name,
            @RequestParam Integer prix,
            @RequestParam Integer stockage,
            @RequestParam Integer ram) {
        return ResponseEntity.ok(abonnementService.addNewAbbo(name, prix, stockage, ram));
    }

    @GetMapping("getUserByUsername")
    public ResponseEntity<?> getUserByUsername(@RequestParam String username) {
        return utilisateurService.getUserByUsername(username);
    }

}
