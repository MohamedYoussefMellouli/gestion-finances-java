package service;

import model.Investissement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestissementService {
    private final String url = "jdbc:postgresql://localhost:5432/testdb";
    private final String user = "postgres";
    private final String password = "youssef";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL non trouvé", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    // 🔹 Récupérer tous les investissements d'un utilisateur
    public List<Investissement> getInvestissementsByUtilisateur(int idUtilisateur) {
        List<Investissement> investissements = new ArrayList<>();
        String sql = "SELECT id_investissement, id_utilisateur_investissement, type, symbole, quantite, prixachatunitaire, dateachat " +
                     "FROM investissement WHERE id_utilisateur_investissement = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Investissement inv = new Investissement();
                inv.setIdInvestissement(rs.getInt("id_investissement"));
                inv.setIdUtilisateurInvestissement(rs.getInt("id_utilisateur_investissement"));
                inv.setType(rs.getString("type"));
                inv.setSymbole(rs.getString("symbole"));
                inv.setQuantite(rs.getDouble("quantite"));
                inv.setPrixAchatUnitaire(rs.getDouble("prixachatunitaire"));
                inv.setDateAchat(rs.getDate("dateachat").toLocalDate());
                investissements.add(inv);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération investissements : " + e.getMessage(), e);
        }
        return investissements;
    }

    // 🔹 Ajouter un investissement
    public void ajouter(Investissement inv) {
        String sql = "INSERT INTO investissement (id_utilisateur_investissement, type, symbole, quantite, prixachatunitaire, dateachat) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, inv.getIdUtilisateurInvestissement());
            stmt.setString(2, inv.getType());
            stmt.setString(3, inv.getSymbole());
            stmt.setDouble(4, inv.getQuantite());
            stmt.setDouble(5, inv.getPrixAchatUnitaire());
            stmt.setDate(6, Date.valueOf(inv.getDateAchat()));
            stmt.executeUpdate();

            // récupérer l'ID généré automatiquement
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                inv.setIdInvestissement(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout investissement: " + e.getMessage(), e);
        }
    }

    // 🔹 Récupérer un investissement par son ID
    public Investissement getById(int id) {
        String sql = "SELECT id_investissement, id_utilisateur_investissement, type, symbole, quantite, prixachatunitaire, dateachat " +
                     "FROM investissement WHERE id_investissement = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Investissement inv = new Investissement();
                inv.setIdInvestissement(rs.getInt("id_investissement"));
                inv.setIdUtilisateurInvestissement(rs.getInt("id_utilisateur_investissement"));
                inv.setType(rs.getString("type"));
                inv.setSymbole(rs.getString("symbole"));
                inv.setQuantite(rs.getDouble("quantite"));
                inv.setPrixAchatUnitaire(rs.getDouble("prixachatunitaire"));
                inv.setDateAchat(rs.getDate("dateachat").toLocalDate());
                return inv;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération investissement par ID: " + e.getMessage(), e);
        }
        return null;
    }

    // 🔹 Mettre à jour un investissement
    public void update(Investissement inv) {
        String sql = "UPDATE investissement SET type = ?, symbole = ?, quantite = ?, prixachatunitaire = ?, dateachat = ? " +
                     "WHERE id_investissement = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, inv.getType());
            stmt.setString(2, inv.getSymbole());
            stmt.setDouble(3, inv.getQuantite());
            stmt.setDouble(4, inv.getPrixAchatUnitaire());
            stmt.setDate(5, Date.valueOf(inv.getDateAchat()));
            stmt.setInt(6, inv.getIdInvestissement());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour investissement: " + e.getMessage(), e);
        }
    }

    // 🔹 Supprimer un investissement
    public void delete(int id) {
        String sql = "DELETE FROM investissement WHERE id_investissement = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression investissement: " + e.getMessage(), e);
        }
    }
}
