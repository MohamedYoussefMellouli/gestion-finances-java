package service;

import model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class UtilisateurService {
    private final String url = "jdbc:postgresql://localhost:5432/testdb";
    private final String user = "postgres";
    private final String password = "youssef";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    // 🔹 Vérifie email + mot de passe
    public boolean verifierConnexion(String email, String motdepasse) {
        Utilisateur u = getUtilisateurByEmail(email);
        if (u == null) return false;
        return BCrypt.checkpw(motdepasse, u.getMotdepasse());
    }

    // 🔹 Récupérer l'utilisateur par email
    public Utilisateur getUtilisateurByEmail(String email) {
        String sql = "SELECT id_utilisateur, nom, prenom, email, motdepasse, deviseprincipale, objectiffinancier, dateinscription FROM utilisateur WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Utilisateur u = new Utilisateur();
                    u.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    u.setNom(rs.getString("nom"));
                    u.setPrenom(rs.getString("prenom"));
                    u.setEmail(rs.getString("email"));
                    u.setMotdepasse(rs.getString("motdepasse"));
                    u.setDevisePrincipale(rs.getString("deviseprincipale"));
                    u.setObjectifFinancier(rs.getString("objectiffinancier"));
                    u.setDateInscription(rs.getDate("dateinscription").toLocalDate());
                    return u;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération : " + e.getMessage(), e);
        }
        return null;
    }

    // 🔹 Ajouter un utilisateur
    public void ajouter(Utilisateur u) {
        String sql = "INSERT INTO utilisateur "
                   + "(nom, prenom, email, motdepasse, deviseprincipale, objectiffinancier, dateinscription) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_utilisateur";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getMotdepasse()); // doit être hashé avant
            stmt.setString(5, u.getDevisePrincipale());
            stmt.setString(6, u.getObjectifFinancier());
            stmt.setDate(7, u.getDateInscription() != null ? Date.valueOf(u.getDateInscription()) : new Date(System.currentTimeMillis()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_utilisateur");
                    u.setIdUtilisateur(id);
                } else {
                    throw new SQLException("L'insertion n'a pas retourné d'ID");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur d'insertion : " + e.getMessage(), e);
        }
    }

    // 🔹 Récupère tous les utilisateurs
    public List<Utilisateur> getTousUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT id_utilisateur, nom, prenom, email, motdepasse, deviseprincipale, objectiffinancier, dateinscription FROM utilisateur";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setIdUtilisateur(rs.getInt("id_utilisateur"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setMotdepasse(rs.getString("motdepasse"));
                u.setDevisePrincipale(rs.getString("deviseprincipale"));
                u.setObjectifFinancier(rs.getString("objectiffinancier"));
                u.setDateInscription(rs.getDate("dateinscription").toLocalDate());
                utilisateurs.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de lecture : " + e.getMessage(), e);
        }
        return utilisateurs;
    }

    // 🔹 Mettre à jour un utilisateur
    public void update(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, motdepasse = ?, deviseprincipale = ?, objectiffinancier = ? WHERE id_utilisateur = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getMotdepasse()); // doit être hashé si modifié
            stmt.setString(5, u.getDevisePrincipale());
            stmt.setString(6, u.getObjectifFinancier());
            stmt.setInt(7, u.getIdUtilisateur());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour utilisateur: " + e.getMessage(), e);
        }
    }

    // 🔹 Supprimer un utilisateur
    public void delete(Utilisateur u) {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, u.getIdUtilisateur());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Aucun utilisateur trouvé avec l'ID : " + u.getIdUtilisateur());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de suppression : " + e.getMessage(), e);
        }
    }

    // 🔹 Hacher et mettre à jour le mot de passe
    public void setPassword(Utilisateur u, String newPassword) {
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        u.setMotdepasse(hashed);
        update(u);
    }
}
