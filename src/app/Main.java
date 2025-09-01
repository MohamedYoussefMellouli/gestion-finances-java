package app;

import model.Transaction;
import model.Investissement;
import model.Utilisateur;
import service.TransactionService;
import service.InvestissementService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class Main extends JFrame {

    private final Utilisateur utilisateurConnecte;
    private final TransactionService transactionService = new TransactionService();
    private final InvestissementService investissementService = new InvestissementService();

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

        // Ajout Sidebar
        add(new Sidebar(this), BorderLayout.WEST);

        // Panel principal par défaut (Accueil)
        add(createHomePanel(), BorderLayout.CENTER);
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
    public void showHomePanel() {
        setContentPanel(createHomePanel());
    }

    public void showTransactionPanel() {
        List<Transaction> transactions = transactionService.getTransactionsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        JTable table = new JTable(new TransactionTableModel(transactions));

        JPanel panel = new JPanel(new BorderLayout());
        JLabel titre = new JLabel("📊 Liste des Transactions");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titre, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        setContentPanel(panel);
    }

    public void showInvestissementPanel() {
        List<Investissement> investissements = investissementService.getInvestissementsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        JTable table = new JTable(new InvestissementTableModel(investissements));

        JPanel panel = new JPanel(new BorderLayout());
        JLabel titre = new JLabel("💼 Liste des Investissements");
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titre, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        setContentPanel(panel);
    }

    public void showModifierComptePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titre = new JLabel("👤 Modifier le compte");
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

    // === Actions centralisées ===
    public void seDeconnecter() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment vous déconnecter ?", "Confirmer déconnexion",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // === Table Models ===
    private static class TransactionTableModel extends AbstractTableModel {
        private final List<Transaction> transactions;
        private final String[] colonnes = {"ID", "Type", "Montant", "Devise", "Catégorie", "Date", "Description"};

        public TransactionTableModel(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @Override
        public int getRowCount() { return transactions.size(); }

        @Override
        public int getColumnCount() { return colonnes.length; }

        @Override
        public String getColumnName(int column) { return colonnes[column]; }

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

    private static class InvestissementTableModel extends AbstractTableModel {
        private final List<Investissement> investissements;
        private final String[] colonnes = {"ID", "Type", "Symbole", "Quantité", "Prix Unitaire", "Date Achat"};

        public InvestissementTableModel(List<Investissement> investissements) {
            this.investissements = investissements;
        }

        @Override
        public int getRowCount() { return investissements.size(); }

        @Override
        public int getColumnCount() { return colonnes.length; }

        @Override
        public String getColumnName(int column) { return colonnes[column]; }

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
                default -> null;
            };
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
