package com.example.ex6.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_abonnement")
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Abonnement")
    private Integer PK_abo;

    @Column(name = "Nom")
    private String nom;

    @Column(name = "prix")
    private Integer prix;

    @Column(name = "stockage")
    private Integer stockage;

    @Column(name = "ram")
    private Integer ram;

    // Getters et Setters
    public Integer getId() {
        return PK_abo;
    }

    public void setId(Integer id) {
        this.PK_abo = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getPK_abo() {
        return PK_abo;
    }

    public Integer getPrix() {
        return prix;
    }

    public Integer getRam() {
        return ram;
    }

    public Integer getStockage() {
        return stockage;
    }

    public void setPK_abo(Integer pK_abo) {
        PK_abo = pK_abo;
    }

    public void setPrix(Integer prix) {
        this.prix = prix;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public void setStockage(Integer stockage) {
        this.stockage = stockage;
    }

}