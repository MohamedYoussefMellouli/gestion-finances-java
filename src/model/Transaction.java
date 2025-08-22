package model;

import java.time.LocalDate;

public class Transaction {
    private int idTransaction;
    private int utilisateurId;
    private String type;
    private double montant;
    private String devise;
    private String categorie;
    private LocalDate dateTransaction;
    private String description;

    
    public Transaction() {}

    public Transaction(int utilisateurId, String type, double montant, String devise, String categorie, LocalDate dateTransaction, String description) {
        this.utilisateurId = utilisateurId;
        this.type = type;
        this.montant = montant;
        this.devise = devise;
        this.categorie = categorie;
        this.dateTransaction = dateTransaction;
        this.description = description;
    }

    // Getters et Setters
    public int getIdTransaction() { return idTransaction; }
    public void setIdTransaction(int idTransaction) { this.idTransaction = idTransaction; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public LocalDate getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDate dateTransaction) { this.dateTransaction = dateTransaction; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return type + " - " + montant + " " + devise + " - " + categorie + " - " + dateTransaction;
    }
}
