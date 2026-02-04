package com.example.REST.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.REST.model.Abonnement;
import com.example.REST.repository.AbonnementRepository;

import jakarta.transaction.Transactional;

@Service
public class AbonnementService {

    private final AbonnementRepository abonnementRepository;

    // Le constructeur pour injecter le repository d'abonnement
    @Autowired
    public AbonnementService(AbonnementRepository abonnementRepository) {
        this.abonnementRepository = abonnementRepository;
    }

    // -----------------------------------------------------------
    // Méthode pour ajouter un nouvel abonnement
    // -----------------------------------------------------------
    @Transactional
    public String addNewAbbo(String name, Integer prix, Integer stockage, Integer ram) {

        // Création d'un nouvel objet Abonnement
        Abonnement newAbo = new Abonnement();

        // On assigne les valeurs reçues en paramètre à notre objet Abonnement
        newAbo.setNom(name); // Nom de l'abonnement
        newAbo.setPrix(prix); // Prix de l'abonnement
        newAbo.setStockage(stockage); // Stockage de l'abonnement
        newAbo.setRam(ram); // RAM de l'abonnement

        // Sauvegarde de l'abonnement dans la base de données
        abonnementRepository.save(newAbo);

        // Retourne un message de succès
        return "Abonnement ajouté avec succès";
    }

    // -----------------------------------------------------------
    // Méthode pour obtenir la liste de tous les abonnements
    // -----------------------------------------------------------
    @Transactional
    public ArrayList<Abonnement> infoOffers() {
        Iterable<Abonnement> abonnements = abonnementRepository.findAll();

        ArrayList<Abonnement> abonnementList = new ArrayList<>();

        abonnements.forEach(abonnementList::add);

        return abonnementList;
    }
}
