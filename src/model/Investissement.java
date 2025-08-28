package model;

import java.time.LocalDate;

public class Investissement {
    private int idInvestissement;
    private int idUtilisateurInvestissement; // clé étrangère vers Utilisateur
    private String type;
    private String symbole;
    private double quantite;
    private double prixAchatUnitaire;
    private LocalDate dateAchat;

    public Investissement() {}

    public Investissement(int idUtilisateurInvestissement, String type, String symbole, double quantite,
                          double prixAchatUnitaire, LocalDate dateAchat) {
        this.idUtilisateurInvestissement = idUtilisateurInvestissement;
        this.type = type;
        this.symbole = symbole;
        this.quantite = quantite;
        this.prixAchatUnitaire = prixAchatUnitaire;
        this.dateAchat = dateAchat;
    }
    // getters et setters
    public int getIdInvestissement() { return idInvestissement; }
    public void setIdInvestissement(int idInvestissement) { this.idInvestissement = idInvestissement; }

    public int getIdUtilisateurInvestissement() { return idUtilisateurInvestissement; }
    public void setIdUtilisateurInvestissement(int idUtilisateurInvestissement) { this.idUtilisateurInvestissement = idUtilisateurInvestissement; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSymbole() { return symbole; }
    public void setSymbole(String symbole) { this.symbole = symbole; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public double getPrixAchatUnitaire() { return prixAchatUnitaire; }
    public void setPrixAchatUnitaire(double prixAchatUnitaire) { this.prixAchatUnitaire = prixAchatUnitaire; }

    public LocalDate getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDate dateAchat) { this.dateAchat = dateAchat; }

    @Override
    public String toString() {
        return "Investissement{" +
                "idInvestissement=" + idInvestissement +
                ", idUtilisateur=" + idUtilisateurInvestissement +
                ", type='" + type + '\'' +
                ", symbole='" + symbole + '\'' +
                ", quantite=" + quantite +
                ", prixAchat=" + prixAchatUnitaire +
                ", dateAchat=" + dateAchat +
                '}';
    }
}
