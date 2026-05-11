package REST.dto;

public class AbonnementDTO {
    private String nom;
    private Double prix;
    private Integer stockage;
    private Integer ram;

    public AbonnementDTO(String nom, Double prix, Integer stockage, Integer ram) {
        this.nom = nom;
        this.prix = prix;
        this.stockage = stockage;
        this.ram = ram;
    }

    // Getters/Setters
    public String getNom() {
        return nom;
    }

    public Integer getRam() {
        return ram;
    }

    public Integer getStockage() {
        return stockage;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public void setStockage(Integer stockage) {
        this.stockage = stockage;
    }

    public Double getPrix() {
        return prix;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }
}
