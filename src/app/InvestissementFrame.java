package app;

import model.Investissement;
import model.Utilisateur;
import service.InvestissementService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class InvestissementFrame extends JFrame {
    private JComboBox<String> typeCombo;
    private JComboBox<String> symboleCombo;
    private JSpinner quantiteSpinner;
    private JTextField prixField;

    private InvestissementService service;
    private Utilisateur utilisateurConnecte;
    private Main parentMain;                   // Référence parent pour rafraîchir
    private Investissement investissement;    // null = ajout, sinon modification

    // 🔹 Constructeur pour AJOUT
    public InvestissementFrame(Utilisateur utilisateurConnecte, Main parentMain) {
        this(utilisateurConnecte, parentMain, null);
    }

    // 🔹 Constructeur pour AJOUT / MODIF
    public InvestissementFrame(Utilisateur utilisateurConnecte, Main parentMain, Investissement investissement) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.parentMain = parentMain;
        this.investissement = investissement;
        service = new InvestissementService();

        setTitle(investissement == null ? "Ajouter Investissement" : "Modifier Investissement");
        setSize(400, 300);
        setLayout(new GridLayout(5, 2, 5, 5));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Type
        add(new JLabel("Type:"));
        String[] types = {"Action", "Obligation", "Crypto", "ETF", "Immobilier"};
        typeCombo = new JComboBox<>(types);
        typeCombo.setEditable(true);
        add(typeCombo);

        // Symbole
        add(new JLabel("Symbole:"));
        String[] symboles = {"AAPL", "GOOGL", "TSLA", "BTC", "ETH", "AMZN", "MSFT"};
        symboleCombo = new JComboBox<>(symboles);
        symboleCombo.setEditable(true);
        add(symboleCombo);

        // Quantité
        add(new JLabel("Quantité:"));
        quantiteSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000000.0, 1.0));
        add(quantiteSpinner);

        // Prix Achat Unitaire
        add(new JLabel("Prix Achat Unitaire:"));
        prixField = new JTextField();
        add(prixField);

        // Bouton Ajouter / Modifier
        JButton actionButton = new JButton(investissement == null ? "Ajouter" : "Mettre à jour");
        add(new JLabel());
        add(actionButton);

        // Pré-remplir si modification
        if (investissement != null) {
            typeCombo.setSelectedItem(investissement.getType());
            symboleCombo.setSelectedItem(investissement.getSymbole());
            quantiteSpinner.setValue(investissement.getQuantite());
            prixField.setText(String.valueOf(investissement.getPrixAchatUnitaire()));
        }

        actionButton.addActionListener(e -> handleInvestissementAction());

        setLocationRelativeTo(null);
    }

    // 🔹 Gestion Ajout / Modification
    private void handleInvestissementAction() {
        try {
            String type = (typeCombo.getSelectedItem() != null) ? ((String) typeCombo.getSelectedItem()).trim() : "";
            String symbole = (symboleCombo.getSelectedItem() != null) ? ((String) symboleCombo.getSelectedItem()).trim() : "";
            double quantite = ((Number) quantiteSpinner.getValue()).doubleValue();
            double prix = Double.parseDouble(prixField.getText().trim());

            if (type.isEmpty() || symbole.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Type et symbole sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (quantite <= 0) {
                JOptionPane.showMessageDialog(this, "La quantité doit être supérieure à 0.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (investissement == null) {
                // Ajout
                Investissement inv = new Investissement(
                    utilisateurConnecte.getIdUtilisateur(),
                    type,
                    symbole,
                    quantite,
                    prix,
                    LocalDate.now() // Date actuelle pour nouvel investissement
                );
                service.ajouter(inv);
                JOptionPane.showMessageDialog(this, "Investissement ajouté !");
            } else {
                // Modification
                investissement.setType(type);
                investissement.setSymbole(symbole);
                investissement.setQuantite(quantite);
                investissement.setPrixAchatUnitaire(prix);

                // 🔹 Assurer que la date n'est jamais null
                if (investissement.getDateAchat() == null) {
                    investissement.setDateAchat(LocalDate.now());
                }

                service.update(investissement);
                JOptionPane.showMessageDialog(this, "Investissement mis à jour !");
            }

            if (parentMain != null) parentMain.refreshInvestissementTable(); // Rafraîchir JTable
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Prix doit être un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}

