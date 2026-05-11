package REST.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "t_user")
public class Utilisateur {

    @Id
    @Column(name = "PK_user")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pk;

    @Column(name = "Nom", length = 50)
    private String name;

    @Column(name = "nbEmeraudes")
    private Integer nbEmeraudes;

    @Column(name = "motDePasse")
    private String motDePasse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Abonnement", nullable = true)
    private Abonnement FK_abo;

    public Utilisateur(String name, Integer nbEmeraude, String motDePasse, Abonnement FK_abo) {
        this.name = name;
        this.nbEmeraudes = nbEmeraude;
        this.motDePasse = motDePasse;
        this.FK_abo = FK_abo;
    }

    public Utilisateur(String name, Integer nbEmeraude, String motDePasse) {
        this.name = name;
        this.nbEmeraudes = nbEmeraude;
        this.motDePasse = motDePasse;
    }

    public Utilisateur() {
        // Obligatoire pour JPA
    }

    public Integer getPK() {
        return pk;
    }

    public void setpk(Integer id) {
        this.pk = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNbEmeraudes() {
        return nbEmeraudes;
    }

    public void setNbEmeraudes(Integer nbEmeraudes) {
        this.nbEmeraudes = nbEmeraudes;
    }

    public void addEmeraudes(Integer nbEmeraudes) {
        this.nbEmeraudes = this.nbEmeraudes + nbEmeraudes;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Abonnement getAbo() {
        return FK_abo;
    }

    public void setAbo(Abonnement abo) {
        this.FK_abo = abo;
    }
}
