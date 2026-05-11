package REST.service;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCrypt;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import REST.dto.AbonnementDTO;
import REST.dto.UserDTO;
import REST.model.Abonnement;
import REST.model.Utilisateur;
import REST.repository.AbonnementRepository;
import REST.repository.UtilisateurRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository userRepository;
    private final AbonnementRepository aboRepository;

    @Autowired
    public UtilisateurService(UtilisateurRepository userRepository, AbonnementRepository aboRepository) {
        this.aboRepository = aboRepository;
        this.userRepository = userRepository;
    }

    public ArrayList<UserDTO> findAllUsers() {
        ArrayList<UserDTO> dtoList = new ArrayList<>();
        for (Utilisateur u : userRepository.findAll()) {
            if (u.getAbo() == null) {
                dtoList.add(new UserDTO(u.getPK(), u.getName(), u.getNbEmeraudes()));
            } else {
                dtoList.add(new UserDTO(u.getPK(), u.getName(), u.getAbo().getNom(), u.getNbEmeraudes()));

            }
        }
        return dtoList;
    }

    @Transactional
    public ResponseEntity<String> addUser(String name, Integer nbEmeraudes, String motDePasse, Integer FK_abo) {
        ResponseEntity<String> retour = ResponseEntity.status(HttpStatus.NOT_FOUND).body("utilisateur not found");
        if (FK_abo != null) {
            Optional<Abonnement> aboOptional = aboRepository.findById(FK_abo);

            if (aboOptional.isPresent()) {
                Abonnement newAbo = aboOptional.get();
                Utilisateur newUser = new Utilisateur(name, nbEmeraudes, hashPassword(motDePasse), newAbo);
                userRepository.save(newUser);
                retour = ResponseEntity.status(HttpStatus.OK)
                        .body("saved avec abo : " + newUser.getAbo());
            } else {
                retour = ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("abonnement not found");
            }
        } else {
            Utilisateur newUser = new Utilisateur(name, nbEmeraudes, hashPassword(motDePasse));
            userRepository.save(newUser);
            retour = ResponseEntity.status(HttpStatus.OK)
                    .body("saved sans abo");
        }

        return retour;

    }

    @Transactional
    public ResponseEntity<String> addEmeraude(Integer PKUser, Integer emeraudes) {
        Optional<Utilisateur> userOptional = userRepository.findById(PKUser);
        ResponseEntity<String> retour = ResponseEntity.status(HttpStatus.NOT_FOUND).body("utilisateur not found");
        if (userOptional.isPresent()) {
            Utilisateur userDB = userOptional.get(); // Récupération de l'utilisateur
            userDB.addEmeraudes(emeraudes); // Modification de l'attribut
            userRepository.save(userDB); // Sauvegarde dans la base
            retour = ResponseEntity.status(HttpStatus.OK).body("Saved");
        }
        return retour;
    }

    @Transactional
    public ResponseEntity<String> acheterAbonnement(Integer PKUser, Integer Fkabonnement) {
        Optional<Utilisateur> userOptional = userRepository.findById(PKUser);
        Optional<Abonnement> aboOptional = aboRepository.findById(Fkabonnement);
        if (userOptional.isPresent() && aboOptional.isPresent()) {
            Utilisateur userDB = userOptional.get();
            Abonnement aboUser = aboOptional.get();
            Integer prix = aboUser.getPrix();
            Integer argentUser = userDB.getNbEmeraudes();
            if (argentUser >= prix) {
                userDB.setAbo(aboUser);
                userDB.setNbEmeraudes(argentUser - prix);
                userRepository.save(userDB);
                String retour = "Abonnement : " + aboUser.getNom() + " acheté pour : " + prix + " emeraudes, plus que "
                        + userDB.getNbEmeraudes() + " emeraudes pour " + userDB.getName();
                return ResponseEntity.status(HttpStatus.OK).body(retour);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Pas suffisamment d'émeraudes");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("utilisateur not found");
        }
    }

    @Transactional
    public ResponseEntity<AbonnementDTO> getOffre(Integer PKUser) {
        ResponseEntity<AbonnementDTO> retour = ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        ;
        Optional<Utilisateur> userOptional = userRepository.findById(PKUser);
        if (userOptional.isPresent()) {
            if (userOptional.get().getAbo() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            }
            Utilisateur userDB = userOptional.get();
            Abonnement abonnementUtilisateur = userDB.getAbo();
            AbonnementDTO dto = new AbonnementDTO(abonnementUtilisateur.getNom(),
                    abonnementUtilisateur.getPrix().doubleValue(), abonnementUtilisateur.getStockage(),
                    abonnementUtilisateur.getRam());
            retour = ResponseEntity.status(HttpStatus.OK).body(dto);
        }
        return retour;
    }

    @Transactional
    public boolean login(String nom, String password) {
        for (Utilisateur utilisateur : userRepository.findAll()) {
            if (utilisateur.getName().equals(nom) && BCrypt.checkpw(password, utilisateur.getMotDePasse())) {
                return true;
            }
        }
        return false;
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    @Transactional
    public ResponseEntity<String> changePassword(Integer PKUtilisateur, String password) {
        Optional<Utilisateur> userOptional = userRepository.findById(PKUtilisateur);
        if (userOptional.isPresent()) {
            Utilisateur userDB = userOptional.get();
            String hashedPassword = hashPassword(password);
            userDB.setMotDePasse(hashedPassword);
            userRepository.save(userDB);
            return ResponseEntity.status(HttpStatus.OK).body("password Changed");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @Transactional
    public ResponseEntity<String> changeName(Integer PKUtilisateur, String name) {
        Optional<Utilisateur> userOptional = userRepository.findById(PKUtilisateur);
        if (userOptional.isPresent()) {
            Utilisateur userDB = userOptional.get();
            userDB.setName(name);
            userRepository.save(userDB);
            return ResponseEntity.status(HttpStatus.OK).body("name Changed");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

    }

    @Transactional
    public ResponseEntity<UserDTO> getUserByUsername(String username) {
        Optional<Utilisateur> userOptional = userRepository.findByName(username);
        if (userOptional.isPresent()) {
            UserDTO userDB;
            if (userOptional.get().getAbo() == null) {
                userDB = new UserDTO(userOptional.get().getPK(), userOptional.get().getName(),
                        userOptional.get().getNbEmeraudes());

            } else {
                userDB = new UserDTO(userOptional.get().getPK(), userOptional.get().getName(),
                        userOptional.get().getAbo().getNom(), userOptional.get().getNbEmeraudes());
            }

            return ResponseEntity.status(HttpStatus.OK).body(userDB);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

    }

}