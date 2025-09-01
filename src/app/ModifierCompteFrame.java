package app;

import model.Utilisateur;
import service.UtilisateurService;

import javax.swing.*;
import java.awt.*;

public class ModifierCompteFrame extends JFrame {
    private JTextField nomField, prenomField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> deviseCombo;
    private JTextField objectifField;

    private UtilisateurService service;
    private Utilisateur utilisateur;
    private Main mainWindow;

    
    public ModifierCompteFrame(Utilisateur utilisateur, Main mainWindow) {
        this.utilisateur = utilisateur;
        this.mainWindow = mainWindow;
        this.service = new UtilisateurService();

        setTitle("Modifier mon compte");
        setSize(400, 350);
        setLayout(new GridLayout(7, 2, 5, 5));

        // Champs du formulaire
        add(new JLabel("Nom:"));
        nomField = new JTextField(utilisateur.getNom());
        add(nomField);

        add(new JLabel("Prénom:"));
        prenomField = new JTextField(utilisateur.getPrenom());
        add(prenomField);

        add(new JLabel("Email:"));
        emailField = new JTextField(utilisateur.getEmail());
        add(emailField);

        add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Devise principale:"));
        String[] devises = {"EUR", "USD", "GBP", "JPY"};
        deviseCombo = new JComboBox<>(devises);
        deviseCombo.setSelectedItem(utilisateur.getDevisePrincipale());
        add(deviseCombo);

        add(new JLabel("Objectif financier:"));
        objectifField = new JTextField(utilisateur.getObjectifFinancier());
        add(objectifField);

        // Bouton Enregistrer
        JButton saveButton = new JButton("Enregistrer les modifications");
        add(new JLabel()); // placeholder
        add(saveButton);

        saveButton.addActionListener(e -> {
            // ✅ Vérification champs obligatoires
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

            // Validation de l'email
            String email = emailField.getText().trim();
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer un email valide (exemple: utilisateur@domaine.com).",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validation du mot de passe (si modifié)
            String newPassword = new String(passwordField.getPassword()).trim();
            if (!newPassword.isEmpty()) {
                if (newPassword.length() < 8) {
                    JOptionPane.showMessageDialog(this,
                            "Le mot de passe doit contenir au moins 8 caractères.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!newPassword.matches(".*[A-Z].*")) {
                    JOptionPane.showMessageDialog(this,
                            "Le mot de passe doit contenir au moins une majuscule.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!newPassword.matches(".*[0-9].*")) {
                    JOptionPane.showMessageDialog(this,
                            "Le mot de passe doit contenir au moins un numéro.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try {
                utilisateur.setNom(nomField.getText().trim());
                utilisateur.setPrenom(prenomField.getText().trim());
                utilisateur.setEmail(emailField.getText().trim());
                utilisateur.setDevisePrincipale((String) deviseCombo.getSelectedItem());
                utilisateur.setObjectifFinancier(objectifField.getText().trim());

                if (!newPassword.isEmpty()) {
                    // 🔹 Hash le mot de passe avant sauvegarde
                    service.setPassword(utilisateur, newPassword);
                } else {
                    // Update sans changer le mot de passe
                    service.update(utilisateur);
                }

                JOptionPane.showMessageDialog(this, "Compte mis à jour avec succès !");
                dispose();

                // Rafraîchir l'affichage dans Main
                mainWindow.refreshUserInfo();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la mise à jour: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setLocationRelativeTo(mainWindow);
    }
}