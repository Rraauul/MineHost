package com.example.ex6.dto;

import com.example.ex6.model.Abonnement;

public class UserDTO {
    private Integer id;
    private String name;

    private String abonnementNom;
    private Integer nbEmeraudes;

    // Nom du pays pour simplifier, vous pouvez inclure d'autres propriétés si
    // nécessaire
    // Constructeurs, getters et setters

    public UserDTO() {
    }

    public UserDTO(Integer id, String name, String abonnementNom, Integer nbEmeraudes) {
        this.id = id;
        this.name = name;

        this.abonnementNom = abonnementNom;
        this.nbEmeraudes = nbEmeraudes;

    }

    public UserDTO(Integer id, String name, Integer nbEmeraudes) {
        this.id = id;
        this.name = name;
        this.nbEmeraudes = nbEmeraudes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNbEmeraudes() {
        return nbEmeraudes;
    }

    public void setNbEmeraudes(Integer nbEmeraudes) {
        this.nbEmeraudes = nbEmeraudes;
    }

    public String getName() {
        return name;
    }

    public String getAbonnementNom() {
        return abonnementNom;
    }

    public void setAbonnementNom(String abonnementNom) {
        this.abonnementNom = abonnementNom;
    }

    public void setName(String name) {
        this.name = name;
    }

}