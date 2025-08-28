package app;

import model.Investissement;
import model.Transaction;
import model.Utilisateur;
import service.InvestissementService;
import service.TransactionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Main extends JFrame {

    private final Utilisateur utilisateurConnecte;
    private final InvestissementService investissementService;
    private final TransactionService transactionService;
    private JTable tableInvestissements;
    private JTable tableTransactions;
    private JButton triAsc, triDesc;
    private String filtreTypeTransaction = "Tous"; // filtre actif

    public Main(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.investissementService = new InvestissementService();
        this.transactionService = new TransactionService();

        initUI();
        setupTables();
        setLocationRelativeTo(null);
    }

    // === Initialisation UI ===
    private void initUI() {
        setTitle("Gestion de " + utilisateurConnecte.getNom());
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Ajout Sidebar avec utilisateurConnecte
        add(new Sidebar(utilisateurConnecte), BorderLayout.WEST);

        // Panel principal avec top panel + tables
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // === Sidebar interne ===
    private static class Sidebar extends JPanel {
        public Sidebar(Utilisateur utilisateurConnecte) {
            setBackground(new Color(45, 45, 45));
            setPreferredSize(new Dimension(200, 0));
            setLayout(new GridLayout(4, 1, 0, 10));

            add(createButton("Modifier Compte", e -> {
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (parent instanceof Main main) main.openModifierCompte();
            }));

            add(createButton("Transactions", e -> {
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                new ListeTransactionFrame(utilisateurConnecte).setVisible(true);
            }));

            add(createButton("Investissements", e -> {
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                new ListeInvestissementFrame(utilisateurConnecte).setVisible(true);
            }));

            add(createButton("Déconnexion", e -> {
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (parent instanceof Main main) main.seDeconnecter();
            }));
        }

        private static JButton createButton(String text, ActionListener listener) {
            JButton btn = new JButton(text);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(70, 70, 70));
            btn.setForeground(Color.WHITE);
            btn.addActionListener(listener);
            return btn;
        }
    }

    // === Top Panel ===
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Tri des investissements
        triAsc = new JButton("Trier ↑");
        triDesc = new JButton("Trier ↓");
        triAsc.addActionListener(e -> trierInvestissements(true));
        triDesc.addActionListener(e -> trierInvestissements(false));
        panel.add(triAsc);
        panel.add(triDesc);

        // Filtre type transaction
        String[] typesTransaction = {"Tous", "Dépense", "Revenu", "Paiement en ligne"};
        JComboBox<String> typeCombo = new JComboBox<>(typesTransaction);
        typeCombo.addActionListener(e -> {
            filtreTypeTransaction = (String) typeCombo.getSelectedItem();
            refreshTransactionTable();
        });
        panel.add(new JLabel("Filtrer par type:"));
        panel.add(typeCombo);

        return panel;
    }

    // === Panel central ===
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        tableInvestissements = new JTable();
        panel.add(new JScrollPane(tableInvestissements));

        tableTransactions = new JTable();
        panel.add(new JScrollPane(tableTransactions));
        return panel;
    }

    // === Actions centralisées ===
    private void openModifierCompte() {
        new ModifierCompteFrame(utilisateurConnecte, this).setVisible(true);
    }

    private void openAjouterTransaction() {
        new TransactionFrame(utilisateurConnecte, this).setVisible(true);
    }

    private void openAjouterInvestissement() {
        new InvestissementFrame(utilisateurConnecte, this).setVisible(true);
    }

    private void seDeconnecter() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter ?", "Confirmer déconnexion",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    public void refreshUserInfo() {
        setTitle("Gestion de " + utilisateurConnecte.getNom());
    }

    // === Gestion des tables ===
    private void trierInvestissements(boolean asc) {
        TableRowSorter<InvestissementTableModel> sorter =
                new TableRowSorter<>((InvestissementTableModel) tableInvestissements.getModel());
        sorter.setSortKeys(List.of(new SortKey(5, asc ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
        tableInvestissements.setRowSorter(sorter);
    }

    private void setupTables() {
        refreshInvestissementTable();
        refreshTransactionTable();
    }

    public void refreshInvestissementTable() {
        List<Investissement> investissements = investissementService
                .getInvestissementsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        refreshInvestissementTable(investissements);
    }

    public void refreshInvestissementTable(List<Investissement> investissements) {
        tableInvestissements.setModel(new InvestissementTableModel(investissements));
        setupTableButtons(tableInvestissements, investissements, 6, 7);
    }

    public void refreshTransactionTable() {
        List<Transaction> transactions = transactionService.getTransactionsByUtilisateur(utilisateurConnecte.getIdUtilisateur());

        // Appliquer le filtre si nécessaire
        if (!"Tous".equals(filtreTypeTransaction)) {
            transactions.removeIf(t -> !t.getType().equals(filtreTypeTransaction));
        }

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Type", "Montant", "Devise", "Catégorie", "Description", "Date", "Modifier", "Supprimer"},
                0
        );

        for (Transaction t : transactions) {
            model.addRow(new Object[]{
                    t.getIdTransaction(),
                    t.getType(),
                    t.getMontant(),
                    t.getDevise(),
                    t.getCategorie(),
                    t.getDescription(),
                    t.getDateTransaction(),
                    "Modifier",
                    "Supprimer"
            });
        }

        tableTransactions.setModel(model);
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
        if (item instanceof Investissement)
            new InvestissementFrame(utilisateurConnecte, this, (Investissement) item).setVisible(true);
        else if (item instanceof Transaction)
        	new TransactionFrame(utilisateurConnecte, this, (Transaction) item).setVisible(true);
    }

    private void handleDeleteAction(Object item) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer cet élément ?", "Confirmer", JOptionPane.YES_NO_OPTION);
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

    // === Table Models ===
    private static class InvestissementTableModel extends AbstractTableModel {
        private final List<Investissement> investissements;
        private final String[] colonnes = {"ID", "Type", "Symbole", "Quantité", "Prix Unitaire", "Date Achat", "Modifier", "Supprimer"};
        public InvestissementTableModel(List<Investissement> investissements) { this.investissements = investissements; }
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

    // === Boutons dans tables ===
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString()); return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private final String label;
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
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row; button.setText(label); clicked = true; return button;
        }
        public Object getCellEditorValue() { if (clicked) action.perform(row); clicked = false; return label; }
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    // === Main ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Utilisateur u = new Utilisateur();
            u.setIdUtilisateur(1);
            u.setNom("Youssef");
            new Main(u).setVisible(true);
        });
    }
}
