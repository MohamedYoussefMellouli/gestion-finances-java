package service;

import model.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
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

    // 🔹 Ajouter une transaction
    public void ajouter(Transaction t) {
        String sql = "INSERT INTO transaction (utilisateurid_transaction, type, montant, devise, categorie, datetransaction, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_transaction";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, t.getUtilisateurId());
            stmt.setString(2, t.getType());
            stmt.setDouble(3, t.getMontant());
            stmt.setString(4, t.getDevise());
            stmt.setString(5, t.getCategorie());
            stmt.setDate(6, Date.valueOf(t.getDateTransaction()));
            stmt.setString(7, t.getDescription());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                t.setIdTransaction(rs.getInt("id_transaction"));
            } else {
                throw new SQLException("Aucun ID retourné pour la transaction");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout transaction : " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactionsByUtilisateur(int idUtilisateur) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE utilisateurid_transaction = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setUtilisateurId(rs.getInt("utilisateurid_transaction"));
                t.setType(rs.getString("type"));
                t.setMontant(rs.getDouble("montant"));
                t.setDevise(rs.getString("devise"));
                t.setCategorie(rs.getString("categorie"));
                t.setDateTransaction(rs.getDate("datetransaction").toLocalDate());
                t.setDescription(rs.getString("description"));
                list.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture transactions : " + e.getMessage(), e);
        }
        return list;
    }

    // 🔹 Récupérer une transaction par ID
    public Transaction getById(int id) {
        String sql = "SELECT * FROM transaction WHERE id_transaction = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setUtilisateurId(rs.getInt("utilisateurid_transaction"));
                t.setType(rs.getString("type"));
                t.setMontant(rs.getDouble("montant"));
                t.setDevise(rs.getString("devise"));
                t.setCategorie(rs.getString("categorie"));
                t.setDateTransaction(rs.getDate("datetransaction").toLocalDate());
                t.setDescription(rs.getString("description"));
                return t;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération transaction par ID : " + e.getMessage(), e);
        }
        return null;
    }

    // 🔹 Mettre à jour une transaction
    public void update(Transaction t) {
        String sql = "UPDATE transaction SET type = ?, montant = ?, devise = ?, categorie = ?, datetransaction = ?, description = ? " +
                     "WHERE id_transaction = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getType());
            stmt.setDouble(2, t.getMontant());
            stmt.setString(3, t.getDevise());
            stmt.setString(4, t.getCategorie());
            stmt.setDate(5, Date.valueOf(t.getDateTransaction()));
            stmt.setString(6, t.getDescription());
            stmt.setInt(7, t.getIdTransaction());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour transaction : " + e.getMessage(), e);
        }
    }

    // 🔹 Récupérer toutes les transactions
    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction ORDER BY datetransaction DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setUtilisateurId(rs.getInt("utilisateurid_transaction"));
                t.setType(rs.getString("type"));
                t.setMontant(rs.getDouble("montant"));
                t.setDevise(rs.getString("devise"));
                t.setCategorie(rs.getString("categorie"));
                t.setDateTransaction(rs.getDate("datetransaction").toLocalDate());
                t.setDescription(rs.getString("description"));
                list.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture transactions : " + e.getMessage(), e);
        }
        return list;
    }

    // 🔹 Supprimer une transaction
    public void delete(int id) {
        String sql = "DELETE FROM transaction WHERE id_transaction = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Aucune ligne supprimée ! ID : " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression transaction : " + e.getMessage(), e);
        }
    }

    // 🔹 Filtrer les transactions par type (Dépense, Revenu, Paiement en ligne)
    public List<Transaction> filterTransactionsByType(int idUtilisateur, String type) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE utilisateurid_transaction = ? AND type = ? ORDER BY datetransaction DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setIdTransaction(rs.getInt("id_transaction"));
                t.setUtilisateurId(rs.getInt("utilisateurid_transaction"));
                t.setType(rs.getString("type"));
                t.setMontant(rs.getDouble("montant"));
                t.setDevise(rs.getString("devise"));
                t.setCategorie(rs.getString("categorie"));
                t.setDateTransaction(rs.getDate("datetransaction").toLocalDate());
                t.setDescription(rs.getString("description"));
                list.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du filtrage des transactions : " + e.getMessage(), e);
        }
        return list;
    }
}