package app;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import model.Transaction;
import model.Utilisateur;
import service.PaiementService;
import service.TransactionService;
import service.PredictionClient;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class TransactionFrame extends JFrame {

    private JComboBox<String> typeCombo;
    private JSpinner montantSpinner;
    private JComboBox<String> deviseCombo;
    private JComboBox<String> categorieCombo;
    private JTextField descriptionField;
    private JButton stripeButton;

    private TransactionService service;
    private Utilisateur utilisateurConnecte;
    private Main mainWindow;
    private Transaction transaction; // null = ajout, sinon modification

    // 🔹 Constructeur pour AJOUT
    public TransactionFrame(Utilisateur utilisateurConnecte, Main mainWindow) {
        this(utilisateurConnecte, mainWindow, null);
    }

    // 🔹 Contrôle de saisie
    private boolean validerChamps() {
        String type = (String) typeCombo.getSelectedItem();
        double montant = ((Number) montantSpinner.getValue()).doubleValue();
        String devise = (String) deviseCombo.getSelectedItem();
        String categorie = (String) categorieCombo.getSelectedItem();
        String description = descriptionField.getText().trim();

        if (type == null || type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez choisir un type.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (montant <= 0) {
            JOptionPane.showMessageDialog(this, "Le montant doit être supérieur à 0.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (devise == null || devise.isEmpty() || devise.length() != 3) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une devise valide (ex: EUR, USD).", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (categorie == null || categorie.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez choisir une catégorie.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (description.length() < 3) {
            JOptionPane.showMessageDialog(this, "La description doit contenir au moins 3 caractères.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // 🔹 Constructeur pour AJOUT / MODIF
    public TransactionFrame(Utilisateur utilisateurConnecte, Main mainWindow, Transaction t) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.mainWindow = mainWindow;
        this.transaction = t;
        service = new TransactionService();

        setTitle(t == null ? "Ajouter Transaction" : "Modifier Transaction");
        setSize(400, 450);
        setLayout(new GridLayout(8, 2, 5, 5));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Type
        add(new JLabel("Type:"));
        String[] types = {"Dépense", "Revenu", "Paiement en ligne"};
        typeCombo = new JComboBox<>(types);
        typeCombo.setEditable(false);
        add(typeCombo);

        // Montant
        add(new JLabel("Montant:"));
        SpinnerNumberModel montantModel = new SpinnerNumberModel(0.0, 0.0, 1000000.0, 1.0);
        montantSpinner = new JSpinner(montantModel);
        add(montantSpinner);

        // Devise
        add(new JLabel("Devise:"));
        String[] devises = {"EUR", "USD", "GBP", "JPY", "CHF", "BTC", "ETH"};
        deviseCombo = new JComboBox<>(devises);
        deviseCombo.setEditable(true);
        add(deviseCombo);

        // Catégorie
        add(new JLabel("Catégorie:"));
        String[] categories = {"Alimentation", "Transport", "Santé", "Divertissement", "Logement", "Éducation", "Paiement en ligne", "Autres"};
        categorieCombo = new JComboBox<>(categories);
        categorieCombo.setEditable(true);
        add(categorieCombo);

        // Description
        add(new JLabel("Description:"));
        descriptionField = new JTextField();
        add(descriptionField);

        // Bouton Ajouter / Modifier
        JButton actionButton = new JButton(t == null ? "Ajouter" : "Modifier");
        add(new JLabel()); // placeholder
        add(actionButton);

        // Bouton Stripe
        stripeButton = new JButton("Payer avec Stripe");
        stripeButton.setVisible(false);
        add(new JLabel()); // placeholder
        add(stripeButton);
        JButton predictButton = new JButton("💡 Prédire Montant");
        add(new JLabel()); // placeholder
        add(predictButton);
        

        // Afficher le bouton Stripe uniquement si type = Paiement en ligne
        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            stripeButton.setVisible("Paiement en ligne".equals(selectedType));
        });

        // 🔹 Action bouton Stripe
        stripeButton.addActionListener(e -> {
            if (!validerChamps()) return; // ✅ Vérification avant paiement
            try {
                // 🔹 Ouvrir fenêtre saisie carte
                CardDialog cardDialog = new CardDialog(this);
                cardDialog.setVisible(true);

                if (!cardDialog.isConfirmed()) {
                    JOptionPane.showMessageDialog(this, "Paiement annulé par l'utilisateur.");
                    return;
                }

                // 🔹 Récupérer infos carte
                String cardNumber = cardDialog.getCardNumber();
                String expDate = cardDialog.getExpDate();
                String cvc = cardDialog.getCvc();

                // Simulation test Stripe
                if (!"4242424242424242".equals(cardNumber.replaceAll(" ", ""))) {
                    JOptionPane.showMessageDialog(this, "Carte test invalide (utilise 4242 4242 4242 4242).");
                    return;
                }

                double montant = ((Number) montantSpinner.getValue()).doubleValue();
                String devise = ((String) deviseCombo.getSelectedItem()).toLowerCase();
                String description = descriptionField.getText().trim();

                PaiementService paiementService = new PaiementService("sk_test_VOTRE_CLE_API");
                PaymentIntent paiement = paiementService.creerPaiement((long)(montant*100), devise, description);

                JOptionPane.showMessageDialog(this,
                        "✅ Paiement Stripe réussi !\nID : " + paiement.getId(),
                        "Confirmation",
                        JOptionPane.INFORMATION_MESSAGE);

                Transaction tPaiement = new Transaction(
                        utilisateurConnecte.getIdUtilisateur(),
                        "Paiement en ligne",
                        montant,
                        devise.toUpperCase(),
                        "Paiement en ligne",
                        LocalDate.now(),
                        description
                );
                service.ajouter(tPaiement);

                mainWindow.refreshTransactionTable();
                dispose();

            } catch (StripeException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur Stripe : " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
     //  Action prédiction via modèle Python
        predictButton.addActionListener(e -> {
            try {
                // Récupérer les infos nécessaires pour la prédiction
                String categorie = (String) categorieCombo.getSelectedItem();
                String devise = (String) deviseCombo.getSelectedItem();
                LocalDate today = LocalDate.now();
                int mois = today.getMonthValue();
                int jour = today.getDayOfMonth();

                // Appel du modèle Python via HTTP
                double prediction = PredictionClient.predireMontant(categorie, mois, jour, devise);

                // Mettre à jour le montant dans le spinner
                montantSpinner.setValue(prediction);

                JOptionPane.showMessageDialog(this,
                        "Montant prédit : " + String.format("%.2f", prediction) + " " + devise,
                        "Prédiction Python",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur prédiction : " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        // 🔹 Action Ajouter / Modifier transaction classique
        actionButton.addActionListener(e -> {
            if (!validerChamps()) return; // ✅ Vérification avant ajout/modif
            try {
                String type = ((String) typeCombo.getSelectedItem()).trim();
                double montant = ((Number) montantSpinner.getValue()).doubleValue();
                String devise = ((String) deviseCombo.getSelectedItem()).trim();
                String categorie = ((String) categorieCombo.getSelectedItem()).trim();
                String description = descriptionField.getText().trim();

                if (transaction == null) {
                    Transaction newTransaction = new Transaction(
                            utilisateurConnecte.getIdUtilisateur(),
                            type, montant, devise, categorie, LocalDate.now(), description
                    );
                    service.ajouter(newTransaction);
                    JOptionPane.showMessageDialog(this, "Transaction ajoutée !");
                } else {
                    transaction.setType(type);
                    transaction.setMontant(montant);
                    transaction.setDevise(devise);
                    transaction.setCategorie(categorie);
                    transaction.setDescription(description);
                    transaction.setDateTransaction(LocalDate.now());
                    service.update(transaction);
                    JOptionPane.showMessageDialog(this, "Transaction modifiée !");
                }

                mainWindow.refreshTransactionTable();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Remplir les champs si modification
        if (t != null) {
            typeCombo.setSelectedItem(t.getType());
            montantSpinner.setValue(t.getMontant());
            deviseCombo.setSelectedItem(t.getDevise());
            categorieCombo.setSelectedItem(t.getCategorie());
            descriptionField.setText(t.getDescription());
            stripeButton.setVisible("Paiement en ligne".equals(t.getType()));
        }

        setLocationRelativeTo(null);
    }
}
