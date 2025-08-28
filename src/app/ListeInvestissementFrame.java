package app;

import model.Investissement;
import model.Utilisateur;
import service.InvestissementService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class ListeInvestissementFrame extends JFrame {

    private JTable tableInvestissements;
    private InvestissementService service;
    private Utilisateur utilisateurConnecte;

    public ListeInvestissementFrame(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.service = new InvestissementService();

        setTitle("Liste des Investissements de " + utilisateurConnecte.getNom());
        setSize(800, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bouton Ajouter
        JButton ajouterButton = new JButton("Ajouter Investissement");
        ajouterButton.addActionListener(e -> {
            // Ouvre InvestissementFrame pour ajout
            new InvestissementFrame(utilisateurConnecte, null, null) {
                @Override
                public void dispose() {
                    super.dispose();
                    refreshTable(); // 🔹 Rafraîchir table après ajout
                }
            }.setVisible(true);
        });
        add(ajouterButton, BorderLayout.NORTH);

        // Table
        tableInvestissements = new JTable();
        tableInvestissements.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshTable();
        add(new JScrollPane(tableInvestissements), BorderLayout.CENTER);

        // Double-clic pour modifier
        tableInvestissements.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tableInvestissements.getSelectedRow();
                    if (row >= 0) {
                        Investissement inv = ((InvestissementTableModel) tableInvestissements.getModel())
                                .getInvestissementAt(row);
                        new InvestissementFrame(utilisateurConnecte, null, inv) {
                            @Override
                            public void dispose() {
                                super.dispose();
                                refreshTable(); // 🔹 Rafraîchir table après modification
                            }
                        }.setVisible(true);
                    }
                }
            }
        });

        setLocationRelativeTo(null);
    }

    public void refreshTable() {
        List<Investissement> investissements = service.getInvestissementsByUtilisateur(utilisateurConnecte.getIdUtilisateur());
        tableInvestissements.setModel(new InvestissementTableModel(investissements));
    }

    // Table model minimal
    private static class InvestissementTableModel extends AbstractTableModel {
        private final List<Investissement> investissements;
        private final String[] colonnes = {"ID", "Type", "Symbole", "Quantité", "Prix Unitaire", "Date Achat"};

        public InvestissementTableModel(List<Investissement> investissements) {
            this.investissements = investissements;
        }

        @Override
        public int getRowCount() {
            return investissements.size();
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

        public Investissement getInvestissementAt(int row) {
            return investissements.get(row);
        }
    }
}
