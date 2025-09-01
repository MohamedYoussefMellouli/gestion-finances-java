package app;

import model.Transaction;
import model.Investissement;
import model.Utilisateur;
import service.TransactionService;
import service.InvestissementService;
import service.UtilisateurService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    private final Utilisateur utilisateurConnecte;

    // Services
    private final TransactionService transactionService = new TransactionService();
    private final InvestissementService investissementService = new InvestissementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    // Tables
    private JTable transactionTable;
    private TransactionTableModel transactionTableModel;
    private JTable investissementTable;
    private InvestissementTableModel investissementTableModel;

    // 🔹 Constructeur
    public Main(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
        initUI();
        setLocationRelativeTo(null);
    }

    // === Initialisation UI ===
    private void initUI() {
        setTitle("Gestion de " + utilisateurConnecte.getNom());
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new Sidebar(this), BorderLayout.WEST);
        add(createHomePanel(), BorderLayout.CENTER);

        // Transactions
        transactionTableModel = new TransactionTableModel(new ArrayList<>());
        transactionTable = new JTable(transactionTableModel);
        configureTable(transactionTable, new ButtonRendererTransaction(), new ButtonEditorTransaction(transactionTableModel, this));
        transactionTable.getColumn("Actions").setPreferredWidth(200);

        // Investissements
        investissementTableModel = new InvestissementTableModel(new ArrayList<>());
        investissementTable = new JTable(investissementTableModel);
        configureTable(investissementTable, new ButtonRendererInvestissement(), new ButtonEditorInvestissement(investissementTableModel, this));
        investissementTable.getColumn("Actions").setPreferredWidth(200);
    }

    // Méthode pour configurer les tables de manière réutilisable
    private void configureTable(JTable table, TableCellRenderer renderer, TableCellEditor editor) {
        table.getColumn("Actions").setCellRenderer(renderer);
        table.getColumn("Actions").setCellEditor(editor);
        table.setRowHeight(30); // Ajuster la hauteur des lignes pour les boutons
        table.setDefaultEditor(Object.class, editor); // Activer l'édition par défaut
    }

    // === Sidebar interne ===
    private static class Sidebar extends JPanel {
        public Sidebar(Main mainFrame) {
            setBackground(new Color(45, 45, 45));
            setPreferredSize(new Dimension(200, 0));
            setLayout(new GridLayout(5, 1, 0, 10));

            add(createButton("Accueil", e -> mainFrame.showHomePanel()));
            add(createButton("Transactions", e -> mainFrame.showTransactionPanel()));
            add(createButton("Investissements", e -> mainFrame.showInvestissementPanel()));
            add(createButton("Modifier Compte", e -> mainFrame.showModifierComptePanel()));
            add(createButton("Déconnexion", e -> mainFrame.seDeconnecter()));
        }

        private static JButton createButton(String text, java.awt.event.ActionListener listener) {
            JButton btn = new JButton(text);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(70, 70, 70));
            btn.setForeground(Color.WHITE);
            btn.addActionListener(listener);
            return btn;
        }
    }

    // === Panels dynamiques ===
    public void showHomePanel() { setContentPanel(createHomePanel()); }

    public void showTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panneau nord avec titre à gauche et filtre à droite
        JPanel northPanel = new JPanel(new GridLayout(1, 2));
        JLabel titre = new JLabel("📊 Liste des Transactions");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setHorizontalAlignment(SwingConstants.LEFT);
        northPanel.add(titre);

        // Panneau de filtre à droite
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String[] types = {"Tous", "Dépense", "Revenu", "Paiement en ligne"};
        JComboBox<String> typeFilter = new JComboBox<>(types);
        JButton filterButton = new JButton("Filtrer");
        
        filterButton.addActionListener(e -> {
            String selectedType = (String) typeFilter.getSelectedItem();
            if ("Tous".equals(selectedType)) {
                refreshTransactionTable();
            } else {
                TransactionService service = new TransactionService();
                List<Transaction> filteredTransactions = service.filterTransactionsByType(utilisateurConnecte.getIdUtilisateur(), selectedType);
                transactionTableModel.setTransactions(filteredTransactions);
                System.out.println("Filtre appliqué pour type : " + selectedType + " avec " + filteredTransactions.size() + " entrées.");
            }
        });
        filterPanel.add(new JLabel("Filtrer par type : "));
        filterPanel.add(typeFilter);
        filterPanel.add(filterButton);
        northPanel.add(filterPanel);

        panel.add(northPanel, BorderLayout.NORTH);

        JButton ajouterBtn = new JButton("Ajouter Transaction");
        ajouterBtn.addActionListener(e -> new TransactionFrame(utilisateurConnecte, this).setVisible(true));
        panel.add(ajouterBtn, BorderLayout.SOUTH);

        panel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        setContentPanel(panel);

        refreshTransactionTable();
    }

    public void showInvestissementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panneau nord avec titre à gauche et filtre à droite
        JPanel northPanel = new JPanel(new GridLayout(1, 2));
        JLabel titre = new JLabel("💼 Liste des Investissements");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setHorizontalAlignment(SwingConstants.LEFT);
        northPanel.add(titre);

        // Panneau de filtre à droite
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String[] sortOptions = {"Tous", "Quantité Croissante", "Quantité Décroissante"};
        JComboBox<String> sortFilter = new JComboBox<>(sortOptions);
        JButton filterButton = new JButton("Filtrer");
        
        filterButton.addActionListener(e -> {
            String selectedOption = (String) sortFilter.getSelectedItem();
            if ("Tous".equals(selectedOption)) {
                refreshInvestissementTable();
            } else {
                InvestissementService service = new InvestissementService();
                boolean ascending = "Quantité Croissante".equals(selectedOption);
                List<Investissement> sortedInvestissements = service.getInvestissementsByUtilisateurSortedByQuantite(
                    utilisateurConnecte.getIdUtilisateur(), ascending);
                investissementTableModel.setInvestissements(sortedInvestissements);
                System.out.println("Filtre appliqué pour : " + selectedOption + " avec " + sortedInvestissements.size() + " entrées.");
            }
        });
        filterPanel.add(new JLabel("Trier par : "));
        filterPanel.add(sortFilter);
        filterPanel.add(filterButton);
        northPanel.add(filterPanel);

        panel.add(northPanel, BorderLayout.NORTH);

        JButton ajouterBtn = new JButton("Ajouter Investissement");
        ajouterBtn.addActionListener(e -> new InvestissementFrame(utilisateurConnecte, this).setVisible(true));
        panel.add(ajouterBtn, BorderLayout.SOUTH);

        panel.add(new JScrollPane(investissementTable), BorderLayout.CENTER);

        setContentPanel(panel);

        refreshInvestissementTable();
    }

    public void showModifierComptePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Titre
        JLabel titre = new JLabel("👤 Modifier le compte");
        titre.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titre, gbc);

        // Nom
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        JTextField nomField = new JTextField(utilisateurConnecte.getNom(), 20);
        panel.add(nomField, gbc);

        // Prénom
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        JTextField prenomField = new JTextField(utilisateurConnecte.getPrenom(), 20);
        panel.add(prenomField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(utilisateurConnecte.getEmail(), 20);
        panel.add(emailField, gbc);

        // Mot de passe
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Devise principale
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Devise principale:"), gbc);
        gbc.gridx = 1;
        String[] devises = {"EUR", "USD", "GBP", "JPY"};
        JComboBox<String> deviseCombo = new JComboBox<>(devises);
        deviseCombo.setSelectedItem(utilisateurConnecte.getDevisePrincipale());
        panel.add(deviseCombo, gbc);

        // Objectif financier
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Objectif financier:"), gbc);
        gbc.gridx = 1;
        JTextField objectifField = new JTextField(utilisateurConnecte.getObjectifFinancier(), 20);
        panel.add(objectifField, gbc);

        // Date d'inscription (lecture seule)
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("Date d'inscription:"), gbc);
        gbc.gridx = 1;
        String dateInscriptionStr = utilisateurConnecte.getDateInscription() != null 
            ? utilisateurConnecte.getDateInscription().toString() 
            : "Non définie";
        JLabel dateInscriptionLabel = new JLabel(dateInscriptionStr);
        panel.add(dateInscriptionLabel, gbc);

        // Bouton Enregistrer
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 8;
        JButton saveButton = new JButton("Enregistrer les modifications");
        panel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            if (nomField.getText().trim().isEmpty() ||
                prenomField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                deviseCombo.getSelectedItem() == null ||
                objectifField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tous les champs sont obligatoires (le mot de passe seulement si vous voulez le changer).",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String email = emailField.getText().trim();
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer un email valide (exemple: utilisateur@domaine.com).",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newPassword = new String(passwordField.getPassword()).trim();
            if (!newPassword.isEmpty()) {
                if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[0-9].*")) {
                    JOptionPane.showMessageDialog(this,
                            "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un numéro.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try {
                utilisateurConnecte.setNom(nomField.getText().trim());
                utilisateurConnecte.setPrenom(prenomField.getText().trim());
                utilisateurConnecte.setEmail(emailField.getText().trim());
                utilisateurConnecte.setDevisePrincipale((String) deviseCombo.getSelectedItem());
                utilisateurConnecte.setObjectifFinancier(objectifField.getText().trim());

                if (!newPassword.isEmpty()) {
                    utilisateurService.setPassword(utilisateurConnecte, newPassword);
                } else {
                    utilisateurService.update(utilisateurConnecte);
                }

                JOptionPane.showMessageDialog(this, "Compte mis à jour avec succès !");
                refreshUserInfo();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la mise à jour: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setContentPanel(panel);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titre = new JLabel("Bienvenue, " + utilisateurConnecte.getNom());
        titre.setFont(new Font("Arial", Font.BOLD, 22));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titre);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        panel.add(new JLabel("Nom : " + utilisateurConnecte.getNom()));
        panel.add(new JLabel("Prénom : " + utilisateurConnecte.getPrenom()));
        panel.add(new JLabel("Email : " + utilisateurConnecte.getEmail()));
        panel.add(new JLabel("Devise principale : " + utilisateurConnecte.getDevisePrincipale()));
        panel.add(new JLabel("Objectif financier : " + utilisateurConnecte.getObjectifFinancier()));
        panel.add(new JLabel("Date d'inscription : " + utilisateurConnecte.getDateInscription()));

        return panel;
    }

    private void setContentPanel(JPanel newPanel) {
        getContentPane().remove(1);
        getContentPane().add(newPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void seDeconnecter() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter ?", "Confirmer déconnexion",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // === Refresh Tables ===
    public void refreshTransactionTable() {
        List<Transaction> toutesTransactions =
                transactionService.getTransactionsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        transactionTableModel.setTransactions(toutesTransactions);
        System.out.println("Tableau des transactions rafraîchi avec " + toutesTransactions.size() + " entrées.");
    }

    public void refreshInvestissementTable() {
        List<Investissement> investissements =
                investissementService.getInvestissementsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        investissementTableModel.setInvestissements(investissements);
        System.out.println("Tableau des investissements rafraîchi avec " + investissements.size() + " entrées.");
    }

    // Nouvelle méthode pour rafraîchir les infos utilisateur
    public void refreshUserInfo() {
        setTitle("Gestion de " + utilisateurConnecte.getNom());
        showHomePanel();
        showModifierComptePanel();
        System.out.println("Infos utilisateur rafraîchies: " + utilisateurConnecte.getNom());
    }

    // === Table Models ===
    private static class TransactionTableModel extends AbstractTableModel {
        private final List<Transaction> transactions;
        private final String[] colonnes = {"ID", "Type", "Montant", "Devise", "Catégorie", "Date", "Description", "Actions"};

        public TransactionTableModel(List<Transaction> transactions) {
            this.transactions = new ArrayList<>(transactions);
        }

        @Override public int getRowCount() { return transactions.size(); }
        @Override public int getColumnCount() { return colonnes.length; }
        @Override public String getColumnName(int column) { return colonnes[column]; }

        @Override
        public Object getValueAt(int row, int col) {
            Transaction t = transactions.get(row);
            if (col == 7) return "Actions";
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

        public void setTransactions(List<Transaction> nouvellesTransactions) {
            transactions.clear();
            transactions.addAll(nouvellesTransactions);
            fireTableDataChanged();
        }

        public Transaction getTransactionAt(int row) { return transactions.get(row); }
        public void removeTransaction(Transaction t) { transactions.remove(t); fireTableDataChanged(); }
    }

    private static class InvestissementTableModel extends AbstractTableModel {
        private final List<Investissement> investissements;
        private final String[] colonnes = {"ID", "Type", "Symbole", "Quantité", "Prix Unitaire", "Date Achat", "Actions"};

        public InvestissementTableModel(List<Investissement> investissements) {
            this.investissements = new ArrayList<>(investissements);
        }

        @Override public int getRowCount() { return investissements.size(); }
        @Override public int getColumnCount() { return colonnes.length; }
        @Override public String getColumnName(int column) { return colonnes[column]; }

        @Override
        public Object getValueAt(int row, int col) {
            Investissement inv = investissements.get(row);
            if (col == 6) return "Actions";
            return switch (col) {
                case 0 -> inv.getIdInvestissement();
                case 1 -> inv.getType();
                case 2 -> inv.getSymbole();
                case 3 -> inv.getQuantite();
                case 4 -> inv.getPrixAchatUnitaire();
                case 5 -> inv.getDateAchat();
                default -> null;
            };
        }

        public void setInvestissements(List<Investissement> nouvelles) {
            investissements.clear();
            investissements.addAll(nouvelles);
            fireTableDataChanged();
        }

        public Investissement getInvestissementAt(int row) { return investissements.get(row); }
        public void removeInvestissement(Investissement inv) { investissements.remove(inv); fireTableDataChanged(); }
    }

    // === Boutons Transactions ===
    private static class ButtonRendererTransaction extends JPanel implements TableCellRenderer {
        private final JButton modifierBtn = new JButton("Modifier");
        private final JButton supprimerBtn = new JButton("Supprimer");

        public ButtonRendererTransaction() {
            setLayout(new GridLayout(1, 2, 5, 0));
            add(modifierBtn);
            add(supprimerBtn);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private static class ButtonEditorTransaction extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton modifierBtn = new JButton("Modifier");
        private final JButton supprimerBtn = new JButton("Supprimer");

        private final TransactionTableModel model;
        private final Main mainFrame;
        private Transaction currentTransaction;

        public ButtonEditorTransaction(TransactionTableModel model, Main mainFrame) {
            this.model = model;
            this.mainFrame = mainFrame;

            panel.setLayout(new GridLayout(1, 2, 5, 0));
            panel.add(modifierBtn);
            panel.add(supprimerBtn);

            modifierBtn.addActionListener(e -> {
                if (currentTransaction != null) {
                    try {
                        new TransactionFrame(mainFrame.utilisateurConnecte, mainFrame, currentTransaction).setVisible(true);
                        mainFrame.refreshTransactionTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel, "Erreur lors de la modification : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
                fireEditingStopped();
            });

            supprimerBtn.addActionListener(e -> {
                if (currentTransaction != null) {
                    int confirm = JOptionPane.showConfirmDialog(panel, "Supprimer cette transaction ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            new TransactionService().delete(currentTransaction.getIdTransaction());
                            model.removeTransaction(currentTransaction);
                            mainFrame.refreshTransactionTable();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel, "Erreur lors de la suppression : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentTransaction = model.getTransactionAt(row);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    // === Boutons Investissements ===
    private static class ButtonRendererInvestissement extends JPanel implements TableCellRenderer {
        private final JButton modifierBtn = new JButton("Modifier");
        private final JButton supprimerBtn = new JButton("Supprimer");

        public ButtonRendererInvestissement() {
            setLayout(new GridLayout(1, 2, 5, 0));
            add(modifierBtn);
            add(supprimerBtn);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private static class ButtonEditorInvestissement extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton modifierBtn = new JButton("Modifier");
        private final JButton supprimerBtn = new JButton("Supprimer");

        private final InvestissementTableModel model;
        private final Main mainFrame;
        private Investissement currentInvestissement;

        public ButtonEditorInvestissement(InvestissementTableModel model, Main mainFrame) {
            this.model = model;
            this.mainFrame = mainFrame;

            panel.setLayout(new GridLayout(1, 2, 5, 0));
            panel.add(modifierBtn);
            panel.add(supprimerBtn);

            modifierBtn.addActionListener(e -> {
                if (currentInvestissement != null) {
                    try {
                        new InvestissementFrame(mainFrame.utilisateurConnecte, mainFrame, currentInvestissement).setVisible(true);
                        mainFrame.refreshInvestissementTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel, "Erreur lors de la modification : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
                fireEditingStopped();
            });

            supprimerBtn.addActionListener(e -> {
                if (currentInvestissement != null) {
                    int confirm = JOptionPane.showConfirmDialog(panel, "Supprimer cet investissement ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            new InvestissementService().delete(currentInvestissement.getIdInvestissement());
                            model.removeInvestissement(currentInvestissement);
                            mainFrame.refreshInvestissementTable();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel, "Erreur lors de la suppression : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentInvestissement = model.getInvestissementAt(row);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    // === Main ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Utilisateur u = new Utilisateur();
            u.setIdUtilisateur(1);
            u.setNom("Youssef");
            u.setPrenom("Mellouli");
            u.setEmail("youssef@example.com");
            u.setDevisePrincipale("TND");
            u.setObjectifFinancier("Épargne 2025");
            new Main(u).setVisible(true);
        });
    }
}