package app;

import model.Investissement;
import model.Transaction;
import model.Utilisateur;
import service.InvestissementService;
import service.TransactionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Main extends JFrame {
    private final Utilisateur utilisateurConnecte;
    private final InvestissementService investissementService;
    private final TransactionService transactionService;
    private JTable tableInvestissements;
    private JTable tableTransactions;

    public Main(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.investissementService = new InvestissementService();
        this.transactionService = new TransactionService();

        initUI();
        setupTables();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setTitle("Gestion de " + utilisateurConnecte.getNom());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel supérieur avec boutons
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panel central avec tableaux
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Bouton Modifier Compte
        JButton modifierCompteButton = new JButton("Modifier mon compte");
        modifierCompteButton.addActionListener(e -> 
            new ModifierCompteFrame(utilisateurConnecte, this).setVisible(true));
        panel.add(modifierCompteButton);

        // Bouton Ajouter Investissement
        JButton ajouterInvestissementButton = new JButton("Ajouter Investissement");
        ajouterInvestissementButton.addActionListener(e -> 
            new InvestissementFrame(utilisateurConnecte, this).setVisible(true));
        panel.add(ajouterInvestissementButton);

        // Bouton Ajouter Transaction
        JButton ajouterTransactionButton = new JButton("Ajouter Transaction");
        ajouterTransactionButton.addActionListener(e -> 
            new TransactionFrame(utilisateurConnecte, this).setVisible(true));
        panel.add(ajouterTransactionButton);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        tableInvestissements = new JTable();
        panel.add(new JScrollPane(tableInvestissements));

        tableTransactions = new JTable();
        panel.add(new JScrollPane(tableTransactions));

        return panel;
    }

    private void setupTables() {
        refreshInvestissementTable();
        refreshTransactionTable();
    }

    public void refreshInvestissementTable() {
        List<Investissement> investissements = investissementService
            .getInvestissementsByUtilisateur(utilisateurConnecte.getIdUtilisateur());

        tableInvestissements.setModel(new InvestissementTableModel(investissements));
        setupTableButtons(tableInvestissements, investissements, 6, 7);
    }

    public void refreshTransactionTable() {
        List<Transaction> transactions = transactionService
            .getTransactionsByUtilisateur(utilisateurConnecte.getIdUtilisateur());

        tableTransactions.setModel(new TransactionTableModel(transactions));
        setupTableButtons(tableTransactions, transactions, 7, 8);
    }

    private <T> void setupTableButtons(JTable table, List<T> items, int editCol, int deleteCol) {
        table.getColumnModel().getColumn(editCol).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(editCol).setCellEditor(new ButtonEditor(
            new JCheckBox(), "Modifier", row -> handleEditAction(items.get(row))));

        table.getColumnModel().getColumn(deleteCol).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(deleteCol).setCellEditor(new ButtonEditor(
            new JCheckBox(), "Supprimer", row -> handleDeleteAction(items.get(row))));
    }

    private void handleEditAction(Object item) {
        if (item instanceof Investissement) {
            new InvestissementFrame(utilisateurConnecte, this, (Investissement) item).setVisible(true);
        } else if (item instanceof Transaction) {
            new TransactionFrame(utilisateurConnecte, this, (Transaction) item).setVisible(true);
        }
    }

    private void handleDeleteAction(Object item) {
        String message = "Voulez-vous vraiment supprimer cet élément ?";
        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirmer", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (item instanceof Investissement) {
                investissementService.delete(((Investissement) item).getIdInvestissement());
                refreshInvestissementTable();
            } else if (item instanceof Transaction) {
                transactionService.delete(((Transaction) item).getIdTransaction());
                refreshTransactionTable();
            }
        }
    }

    public void refreshUserInfo() {
        setTitle("Gestion de " + utilisateurConnecte.getNom());
    }

    // Classes internes pour les modèles de table
    private static class InvestissementTableModel extends AbstractTableModel {
        private final List<Investissement> investissements;
        private final String[] colonnes = {"ID", "Type", "Symbole", "Quantité", "Prix Unitaire", "Date Achat", "Modifier", "Supprimer"};

        public InvestissementTableModel(List<Investissement> investissements) {
            this.investissements = investissements;
        }

        @Override public int getRowCount() { return investissements.size(); }
        @Override public int getColumnCount() { return colonnes.length; }
        @Override public String getColumnName(int column) { return colonnes[column]; }
        @Override public boolean isCellEditable(int row, int col) { return col >= 6; }

        @Override
        public Object getValueAt(int row, int col) {
            Investissement inv = investissements.get(row);
            return switch (col) {
                case 0 -> inv.getIdInvestissement();
                case 1 -> inv.getType();
                case 2 -> inv.getSymbole();
                case 3 -> inv.getQuantite();
                case 4 -> inv.getPrixAchatUnitaire();
                case 5 -> inv.getDateAchat();
                case 6 -> "Modifier";
                case 7 -> "Supprimer";
                default -> null;
            };
        }
    }

    private static class TransactionTableModel extends AbstractTableModel {
        private final List<Transaction> transactions;
        private final String[] colonnes = {"ID", "Type", "Montant", "Devise", "Catégorie", "Date", "Description", "Modifier", "Supprimer"};

        public TransactionTableModel(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @Override public int getRowCount() { return transactions.size(); }
        @Override public int getColumnCount() { return colonnes.length; }
        @Override public String getColumnName(int column) { return colonnes[column]; }
        @Override public boolean isCellEditable(int row, int col) { return col >= 7; }

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
                case 7 -> "Modifier";
                case 8 -> "Supprimer";
                default -> null;
            };
        }
    }

    // Classes pour les boutons dans les tables (inchangées)
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean clicked;
        private final RowAction action;
        private int row;

        public interface RowAction { void perform(int row); }

        public ButtonEditor(JCheckBox checkBox, String label, RowAction action) {
            super(checkBox);
            this.button = new JButton();
            this.button.setOpaque(true);
            this.label = label;
            this.action = action;
            button.addActionListener((ActionEvent e) -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {
            this.row = row;
            button.setText(label);
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) action.perform(row);
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Pour tester sans connexion
            Utilisateur u = new Utilisateur();
            u.setIdUtilisateur(1);
            u.setNom("Youssef");
            new Main(u).setVisible(true);
        });
    }
}