package app;

import model.Transaction;
import model.Utilisateur;
import service.TransactionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class ListeTransactionFrame extends JFrame {

    private JTable tableTransactions;
    private TransactionService service;
    private Utilisateur utilisateurConnecte;

    public ListeTransactionFrame(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.service = new TransactionService();

        setTitle("Liste des Transactions de " + utilisateurConnecte.getNom());
        setSize(800, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bouton Ajouter
        JButton ajouterButton = new JButton("Ajouter Transaction");
        ajouterButton.addActionListener(e -> {
            // Ouvre TransactionFrame avec rafraîchissement automatique
            new TransactionFrame(utilisateurConnecte, this).setVisible(true);
        });
        add(ajouterButton, BorderLayout.NORTH);

        // Table
        tableTransactions = new JTable();
        refreshTable();
        add(new JScrollPane(tableTransactions), BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    public void refreshTable() {
        List<Transaction> transactions = service.getTransactionsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        tableTransactions.setModel(new TransactionTableModel(transactions));
    }

    // Table model minimal
    private static class TransactionTableModel extends AbstractTableModel {
        private final List<Transaction> transactions;
        private final String[] colonnes = {"ID", "Type", "Montant", "Devise", "Catégorie", "Date", "Description"};

        public TransactionTableModel(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @Override
        public int getRowCount() {
            return transactions.size();
        }

        @Override
        public int getColumnCount() {
            return colonnes.length;
        }

        @Override
        public String getColumnName(int column) {
            return colonnes[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Transaction t = transactions.get(row);
            return switch (col) {
                case 0 -> t.getIdTransaction();
                case 1 -> t.getType();
                case 2 -> t.getMontant();
                case 3 -> t.getDevise();
                case 4 -> t.getCategorie();
                case 5 -> t.getDateTransaction();
                case 6 -> t.getDescription();
                default -> null;
            };
        }
    }
}
