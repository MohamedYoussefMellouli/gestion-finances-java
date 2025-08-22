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
    private Main mainWindow;
    private Investissement investissementToUpdate; // ✅ si modification

    // 🔹 Constructeur pour AJOUT
    public InvestissementFrame(Utilisateur utilisateurConnecte, Main mainWindow) {
        this(utilisateurConnecte, mainWindow, null);
    }

    // 🔹 Constructeur pour UPDATE
    public InvestissementFrame(Utilisateur utilisateurConnecte, Main mainWindow, Investissement investissement) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.mainWindow = mainWindow;
        this.investissementToUpdate = investissement;
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
        SpinnerNumberModel quantiteModel = new SpinnerNumberModel(1.0, 0.0, 1000000.0, 1.0);
        quantiteSpinner = new JSpinner(quantiteModel);
        add(quantiteSpinner);

        // Prix
        add(new JLabel("Prix Achat Unitaire:"));
        prixField = new JTextField();
        add(prixField);

        // Bouton
        JButton actionButton = new JButton(investissement == null ? "Ajouter" : "Mettre à jour");
        add(new JLabel());
        add(actionButton);

        // 🔹 Remplir champs si Update
        if (investissement != null) {
            typeCombo.setSelectedItem(investissement.getType());
            symboleCombo.setSelectedItem(investissement.getSymbole());
            quantiteSpinner.setValue(investissement.getQuantite());
            prixField.setText(String.valueOf(investissement.getPrixAchatUnitaire()));
        }

        actionButton.addActionListener(e -> {
            try {
                String type = ((String) typeCombo.getSelectedItem()).trim();
                String symbole = ((String) symboleCombo.getSelectedItem()).trim();
                double quantite = ((Number) quantiteSpinner.getValue()).doubleValue();
                double prix = Double.parseDouble(prixField.getText().trim());

                // 🔹 Contrôle supplémentaire : quantité > 0
                if (quantite <= 0) {
                    JOptionPane.showMessageDialog(this, "La quantité doit être supérieure à 0.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (type.isEmpty() || symbole.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Type et symbole sont obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (investissementToUpdate == null) {
                    // ✅ Ajout
                    Investissement inv = new Investissement(
                            utilisateurConnecte.getIdUtilisateur(),
                            type, symbole, quantite, prix, LocalDate.now()
                    );
                    service.ajouter(inv);
                    JOptionPane.showMessageDialog(this, "Investissement ajouté !");
                } else {
                    // ✅ Update
                    investissementToUpdate.setType(type);
                    investissementToUpdate.setSymbole(symbole);
                    investissementToUpdate.setQuantite(quantite);
                    investissementToUpdate.setPrixAchatUnitaire(prix);
                    service.update(investissementToUpdate);
                    JOptionPane.showMessageDialog(this, "Investissement mis à jour !");
                }

                mainWindow.refreshInvestissementTable();
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Prix doit être un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        setLocationRelativeTo(null);
    }
}
