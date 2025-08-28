package app;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
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
    private Transaction transaction; // null = ajout, sinon modification

    private ListeTransactionFrame parentListeFrame; // si parent = ListeTransactionFrame
    private Main parentMainFrame;                   // si parent = Main

    // 🔹 Constructeur pour AJOUT avec ListeTransactionFrame
    public TransactionFrame(Utilisateur utilisateurConnecte, ListeTransactionFrame parentFrame) {
        this(utilisateurConnecte, parentFrame, null);
    }

    // 🔹 Constructeur pour AJOUT / MODIF avec ListeTransactionFrame
    public TransactionFrame(Utilisateur utilisateurConnecte, ListeTransactionFrame parentFrame, Transaction t) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.parentListeFrame = parentFrame;
        this.transaction = t;
        initUI();
    }

    // 🔹 Constructeur pour AJOUT avec Main
    public TransactionFrame(Utilisateur utilisateurConnecte, Main mainFrame) {
        this(utilisateurConnecte, mainFrame, null);
    }

    // 🔹 Constructeur pour AJOUT / MODIF avec Main
    public TransactionFrame(Utilisateur utilisateurConnecte, Main mainFrame, Transaction t) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.parentMainFrame = mainFrame;
        this.transaction = t;
        initUI();
    }

    // 🔹 Initialisation UI commune
    private void initUI() {
        service = new TransactionService();

        setTitle(transaction == null ? "Ajouter Transaction" : "Modifier Transaction");
        setSize(400, 500);
        setLayout(new GridLayout(10, 2, 5, 5));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Type
        add(new JLabel("Type:"));
        String[] types = {"Dépense", "Revenu", "Paiement en ligne"};
        typeCombo = new JComboBox<>(types);
        add(typeCombo);

        // Montant
        add(new JLabel("Montant:"));
        montantSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 1.0));
        add(montantSpinner);

        // Devise
        add(new JLabel("Devise:"));
        String[] devises = {"EUR", "USD", "GBP", "JPY", "CHF", "BTC", "ETH"};
        deviseCombo = new JComboBox<>(devises);
        add(deviseCombo);

        // Catégorie
        add(new JLabel("Catégorie:"));
        String[] categories = {"Alimentation", "Transport", "Santé", "Divertissement", "Logement",
                "Éducation", "Paiement en ligne", "Autres"};
        categorieCombo = new JComboBox<>(categories);
        add(categorieCombo);

        // Description
        add(new JLabel("Description:"));
        descriptionField = new JTextField();
        add(descriptionField);

        // Bouton Ajouter / Modifier
        JButton actionButton = new JButton(transaction == null ? "Ajouter" : "Modifier");
        add(new JLabel());
        add(actionButton);

        // Bouton Stripe
        stripeButton = new JButton("Payer avec Stripe");
        stripeButton.setVisible(false); // visible uniquement si type = Paiement en ligne
        add(new JLabel());
        add(stripeButton);

        // Bouton prédiction
        JButton predictButton = new JButton("💡 Prédire Montant");
        add(new JLabel());
        add(predictButton);

        // 🔹 Afficher Stripe seulement si type = Paiement en ligne
        typeCombo.addActionListener(e -> stripeButton.setVisible("Paiement en ligne".equals(typeCombo.getSelectedItem())));

        // 🔹 Action Ajouter / Modifier transaction classique
        actionButton.addActionListener(e -> handleTransaction(false));

        // 🔹 Action Stripe
        stripeButton.addActionListener(e -> handleTransaction(true));

        // 🔹 Action prédiction
        predictButton.addActionListener(e -> {
            try {
                String categorie = (String) categorieCombo.getSelectedItem();
                String devise = (String) deviseCombo.getSelectedItem();
                LocalDate today = LocalDate.now();
                int mois = today.getMonthValue();
                int jour = today.getDayOfMonth();

                double prediction = PredictionClient.predireMontant(categorie, mois, jour, devise);
                montantSpinner.setValue(prediction);

                JOptionPane.showMessageDialog(this,
                        "Montant prédit : " + String.format("%.2f", prediction) + " " + devise,
                        "Prédiction Python",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur prédiction : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Pré-remplir les champs si modification
        if (transaction != null) {
            typeCombo.setSelectedItem(transaction.getType());
            montantSpinner.setValue(transaction.getMontant());
            deviseCombo.setSelectedItem(transaction.getDevise());
            categorieCombo.setSelectedItem(transaction.getCategorie());
            descriptionField.setText(transaction.getDescription());
            stripeButton.setVisible("Paiement en ligne".equals(transaction.getType()));
        }

        setLocationRelativeTo(null);
    }

    // 🔹 Validation simple
    private boolean validerChamps() {
        if (typeCombo.getSelectedItem() == null) return false;
        if (((Number) montantSpinner.getValue()).doubleValue() <= 0) return false;
        if (deviseCombo.getSelectedItem() == null || ((String) deviseCombo.getSelectedItem()).length() != 3) return false;
        if (categorieCombo.getSelectedItem() == null) return false;
        return descriptionField.getText().trim().length() >= 3;
    }

    // 🔹 Gestion Ajout/Modification / Stripe
    private void handleTransaction(boolean stripe) {
        if (!validerChamps()) return;

        String type = (String) typeCombo.getSelectedItem();
        double montant = ((Number) montantSpinner.getValue()).doubleValue();
        String devise = (String) deviseCombo.getSelectedItem();
        String categorie = (String) categorieCombo.getSelectedItem();
        String description = descriptionField.getText().trim();

        try {
            if (stripe) {
                CardDialog cardDialog = new CardDialog(this);
                cardDialog.setVisible(true);
                if (!cardDialog.isConfirmed()) {
                    JOptionPane.showMessageDialog(this, "Paiement annulé par l'utilisateur.");
                    return;
                }

                String cardNumber = cardDialog.getCardNumber();
                if (!"4242424242424242".equals(cardNumber.replaceAll(" ", ""))) {
                    JOptionPane.showMessageDialog(this, "Carte test invalide (utilise 4242 4242 4242 4242).");
                    return;
                }

                PaiementService paiementService = new PaiementService();
                PaymentIntent paiement = paiementService.creerPaiement((long) (montant * 100), devise.toLowerCase(), description);

                JOptionPane.showMessageDialog(this,
                        "✅ Paiement Stripe réussi !\nID : " + paiement.getId(),
                        "Confirmation",
                        JOptionPane.INFORMATION_MESSAGE);

                transaction = new Transaction(utilisateurConnecte.getIdUtilisateur(),
                        "Paiement en ligne", montant, devise.toUpperCase(),
                        "Paiement en ligne", LocalDate.now(), description);

                service.ajouter(transaction);

            } else {
                if (transaction == null) {
                    transaction = new Transaction(utilisateurConnecte.getIdUtilisateur(),
                            type, montant, devise, categorie, LocalDate.now(), description);
                    service.ajouter(transaction);
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
            }

            // 🔹 Rafraîchir la table selon le parent
            if (parentListeFrame != null) parentListeFrame.refreshTable();
            if (parentMainFrame != null) parentMainFrame.refreshTransactionTable();

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
