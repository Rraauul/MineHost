package com.example.APIGateWay.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.APIGateWay.dto.LoginRequestDTO;
import com.example.APIGateWay.service.RestAPI1;
import com.example.APIGateWay.service.RestAPI2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = { "https://fuentesr.emf-informatique.ch",
        "https://tornarem.emf-informatique.ch", "https://apigwfuenter.emf-informatique.ch" }, allowCredentials = "true")

@RestController
public class Controller {
    private final RestAPI1 restAPI1;
    private final RestAPI2 restAPI2;

    public Controller(RestAPI1 restAPI1, RestAPI2 restAPI2) {
        this.restAPI1 = restAPI1;
        this.restAPI2 = restAPI2;
    }

    @PostMapping("/createWorld")
    public ResponseEntity<String> createWorld(HttpSession session,

            @RequestParam String name,

            @RequestParam String template,

            @RequestParam Integer ram,

            @RequestParam Integer serverId) {
        if (session.getAttribute("user") != null) {

            String username = (String) session.getAttribute("user");

            return restAPI1.createWorld(name, template, ram,
                    restAPI2.getUserIdByUsername(username), serverId);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @PostMapping("/startWorld")
    public ResponseEntity<String> startWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            ResponseEntity<String> listResponse = restAPI1.getWorldsByUserId(restAPI2.getUserIdByUsername(username));

            if (listResponse.getStatusCode().is2xxSuccessful() && listResponse.getBody() != null) {
                String worldList = listResponse.getBody();
                if (worldList.contains("\"id\":" + worldId)) { // Assuming the world ID is in JSON format
                    return restAPI1.startWorld(worldId);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\":\"Ce n'est pas ton monde\"}");
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erreur lors de la récupération des mondes\"}");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Utilisateur non connecté\"}");
    }

    @GetMapping("/getWorldsByUserId")
    public ResponseEntity<String> getWorldsByUser(HttpSession session) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            return restAPI1.getWorldsByUserId(restAPI2.getUserIdByUsername(username));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
    }

    @PostMapping("/stopWorld")
    public ResponseEntity<String> stopWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            ResponseEntity<String> listResponse = restAPI1.getWorldsByUserId(restAPI2.getUserIdByUsername(username));

            if (listResponse.getStatusCode().is2xxSuccessful() && listResponse.getBody() != null) {
                String worldList = listResponse.getBody();
                if (worldList.contains("\"id\":" + worldId)) { // Assuming the world ID is in JSON format
                    return restAPI1.stopWorld(worldId);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\":\"Ce n'est pas ton monde\"}");
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erreur lors de la récupération des mondes\"}");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Utilisateur non connecté\"}");
    }

    @DeleteMapping("/deleteWorld")
    public ResponseEntity<String> deleteWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            ResponseEntity<String> listResponse = restAPI1.getWorldsByUserId(restAPI2.getUserIdByUsername(username));

            if (listResponse.getStatusCode().is2xxSuccessful() && listResponse.getBody() != null) {
                String worldList = listResponse.getBody();
                if (worldList.contains("\"id\":" + worldId)) { // Assuming the world ID is in JSON format
                    return restAPI1.deleteWorld(worldId);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\":\"Ce n'est pas ton monde\"}");
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erreur lors de la récupération des mondes\"}");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Utilisateur non connecté\"}");
    }

    @GetMapping("/infoWorld")
    public ResponseEntity<String> getInfoWorld(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            ResponseEntity<String> listResponse = restAPI1.getWorldsByUserId(restAPI2.getUserIdByUsername(username));

            if (listResponse.getStatusCode().is2xxSuccessful() && listResponse.getBody() != null) {
                String worldList = listResponse.getBody();
                if (worldList.contains("\"id\":" + worldId)) { // Assuming the world ID is in JSON format
                    return restAPI1.getInfoWorld(worldId);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\":\"Ce n'est pas ton monde\"}");
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Erreur lors de la récupération des mondes\"}");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Utilisateur non connecté\"}");
    }

    @GetMapping("/downloadLog")
    public ResponseEntity<byte[]> downloadLog(HttpSession session, @RequestParam Integer worldId) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            ResponseEntity<String> listResponse = restAPI1.getWorldsByUserId(restAPI2.getUserIdByUsername(username));

            if (listResponse.getStatusCode().is2xxSuccessful() && listResponse.getBody() != null) {
                String worldList = listResponse.getBody();
                if (worldList.contains("\"id\":" + worldId)) { // Assuming the world ID is in JSON format
                    return restAPI1.downloadLog(worldId);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @GetMapping("/infoServer")
    public ResponseEntity<String> getServerInfo(@RequestParam Integer serverId) {
        return restAPI1.getServerInfo(serverId);
    }

    @GetMapping("/getTemplates")
    public ResponseEntity<String> getTemplates() {
        return restAPI1.getTemplates();
    }

    @GetMapping("/infoOffers")
    public ResponseEntity<String> getInfoOffers() {
        return restAPI2.getInfoOffers();
    }

    @GetMapping("/offerByClient")
    public ResponseEntity<String> getOfferByClient(HttpSession session) {
        if (session.getAttribute("user") != null) {
            String username = (String) session.getAttribute("user");
            Integer pkUser = restAPI2.getUserIdByUsername(username);
            if (pkUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }
            return restAPI2.getOfferByClient(pkUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<String> addUser(@RequestParam String nom,
            @RequestParam String motDePasse,
            @RequestParam(required = false) Integer FK_abo) {
        return restAPI2.addUser(nom, 0, motDePasse, FK_abo);
    }

    @PostMapping("/addEmeraude")
    public ResponseEntity<String> addEmeraude(@RequestParam Integer pkUser,
            @RequestParam Integer emeraudes) {

        return restAPI2.addEmeraude(pkUser, emeraudes);
    }

    @PostMapping("/acheterAbo")
    public ResponseEntity<String> acheterAbo(HttpSession session, @RequestParam Integer pkUser,
            @RequestParam Integer Fkabonnement) {

        if (session.getAttribute("pkUser") == pkUser) {
            return restAPI2.acheterAbo(pkUser, Fkabonnement);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(HttpSession session,
            @RequestBody LoginRequestDTO loginRequest) {
        ResponseEntity<String> response = restAPI2.login(loginRequest.getName(), loginRequest.getMotDePasse());
        if (response.getStatusCode().is2xxSuccessful()) {
            session.setAttribute("user", loginRequest.getName()); // gérer session dans Gateway

            session.setAttribute("pkUser", restAPI2.getUserIdByUsername(loginRequest.getName()));

        }
        return response;
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

    @GetMapping("/getAllUsers")
    public ResponseEntity<String> getAllUsers() {
        return restAPI2.getAllUsers();
    }

    @PutMapping("/changeName")
    public ResponseEntity<String> changeName(@RequestParam Integer PK,
            @RequestParam String newName) {
        return restAPI2.changeName(PK, newName);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestParam Integer PK,
            @RequestParam String password) {
        return restAPI2.changePassword(PK, password);
    }

    @PostMapping("/addAbo")
    public ResponseEntity<String> addAbo(@RequestParam String name,
            @RequestParam Integer prix,
            @RequestParam Integer stockage,
            @RequestParam Integer ram) {
        return restAPI2.addAbo(name, prix, stockage, ram);
    }

    @GetMapping("getUserByUsername")
    public ResponseEntity<String> getUserByUsername(@RequestParam String username) {
        return restAPI2.getUserByUsername(username);
    }

}
