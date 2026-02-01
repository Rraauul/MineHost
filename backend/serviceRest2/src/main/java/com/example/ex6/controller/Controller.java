package com.example.ex6.controller;

import com.example.ex6.dto.AbonnementDTO;
import com.example.ex6.dto.LoginRequestDTO;
import com.example.ex6.dto.UserDTO;
import com.example.ex6.model.Abonnement;
import com.example.ex6.model.Utilisateur;
import com.example.ex6.service.AbonnementService;
import com.example.ex6.service.UtilisateurService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class Controller {

    private final UtilisateurService user;
    private final AbonnementService abo;

    @Autowired
    public Controller(UtilisateurService user, AbonnementService abo) {
        this.abo = abo;
        this.user = user;
    }

    // Handler pour GET
    @GetMapping("/")
    public String getNothing() {
        return "";
    }

    @PostMapping(path = "/addUser")
    public ResponseEntity<String> addUser(@RequestParam String nom, @RequestParam Integer nbEmeraudes,
            @RequestParam String motDePasse, Integer FK_abo) {
        return user.addUser(nom, nbEmeraudes, motDePasse, FK_abo);
    }

    @PostMapping(path = "/addEmeraude")
    public ResponseEntity<String> addEmeraude(@RequestParam Integer pkUser, @RequestParam Integer emeraudes) {
        return user.addEmeraude(pkUser, emeraudes);
    }

    @PostMapping(path = "/acheterAbo")
    public ResponseEntity<String> acheterAbonnement(@RequestParam Integer pkUser, @RequestParam Integer Fkabonnement) {
        return user.acheterAbonnement(pkUser, Fkabonnement);
    }

    @GetMapping(path = "/infoOffers")
    public ResponseEntity<ArrayList<Abonnement>> infoOffers() {
        ArrayList<Abonnement> abos = abo.infoOffers();

        if (abos.isEmpty()) {
            // Si la liste des abonnements est vide, on renvoie un code HTTP NO_CONTENT
            // (204)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            // Si la liste des abonnements n'est pas vide, on renvoie un code HTTP OK (200)
            // avec les abonnements en JSON
            return ResponseEntity.status(HttpStatus.OK).body(abos);
        }
    }

    @GetMapping(path = "/OfferByClient")
    public ResponseEntity<AbonnementDTO> OfferByClient(@RequestParam Integer pkUser) {
        return user.getOffre(pkUser);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO loginRequest) {
        boolean test = user.login(loginRequest.getName(), loginRequest.getMotDePasse());
        Map<String, String> response = new HashMap<>();

        if (test) {
            response.put("message", "Connexion réussie");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response.put("message", "Mot de passe ou utilisateur incorrect (CTRL)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping(path = "/getAllUsers")
    public @ResponseBody Iterable<UserDTO> getAllUsers() {
        return user.findAllUsers();
    }

    @PutMapping(path = "/changeName")
    public ResponseEntity<String> changeName(Integer PK, String newName) {
        return user.changeName(PK, newName);
    }

    @PutMapping(path = "/changePassword")
    public ResponseEntity<String> changePassword(Integer PK, String password) {
        return user.changePassword(PK, password);
    }

    @PostMapping(path = "/addAbo")
    public @ResponseBody String addAbo(String name, Integer prix, Integer stockage, Integer ram) {
        return abo.addNewAbbo(name, prix, stockage, ram);
    }

    @GetMapping(path = "/getUserByUsername")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        return user.getUserByUsername(username);
    }
}