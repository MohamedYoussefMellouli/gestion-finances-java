package model;

import java.time.LocalDate;

public class Utilisateur {
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motdepasse;
    private String devisePrincipale;    // <-- ici : String, pas JComboBox
    private String objectifFinancier;
    private LocalDate dateInscription;
    private Utilisateur utilisateurConnecte;

    // Constructeur complet
    public Utilisateur(String nom, String prenom, String email, String motdepasse,
            String devisePrincipale, String objectifFinancier, LocalDate dateInscription) {
this.nom = nom;
this.prenom = prenom;
this.email = email;
this.motdepasse = motdepasse;
this.devisePrincipale = devisePrincipale;
this.objectifFinancier = objectifFinancier;
this.dateInscription = dateInscription;
}

    public Main(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        initComponents(); 
    }


    public Utilisateur() {
    }

    // Constructeur sans id (création avant insertion en base)
    public Utilisateur(String nom, String prenom, String email, String motdepasse,
                       String devisePrincipale, String objectifFinancier) {
        this(0, nom, prenom, email, motdepasse, devisePrincipale, objectifFinancier, LocalDate.now());
    }

    // Getters et Setters

    
    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotdepasse() {
        return motdepasse;
    }

    public void setMotdepasse(String motdepasse) {
        this.motdepasse = motdepasse;
    }

    public String getDevisePrincipale() {
        return devisePrincipale;
    }

    public void setDevisePrincipale(String devisePrincipale) {
        this.devisePrincipale = devisePrincipale;
    }

    public String getObjectifFinancier() {
        return objectifFinancier;
    }

    public void setObjectifFinancier(String objectifFinancier) {
        this.objectifFinancier = objectifFinancier;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", devisePrincipale='" + devisePrincipale + '\'' +
                ", objectifFinancier='" + objectifFinancier + '\'' +
                ", dateInscription=" + dateInscription +
                '}';
    }
}
