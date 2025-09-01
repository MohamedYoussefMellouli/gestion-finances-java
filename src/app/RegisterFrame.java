package app;

import model.Utilisateur;
import service.UtilisateurService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterFrame extends JFrame {
    private JTextField nomField, prenomField, emailField, objectifField;
    private JPasswordField passwordField;
    private JComboBox<String> deviseCombo;
    private UtilisateurService service;

    public RegisterFrame(JFrame parent) {
        service = new UtilisateurService();

        setTitle("Créer un compte");
        setSize(350, 250);
        setLayout(new GridLayout(7, 2, 5, 5)); // 7 lignes au lieu de 6
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Nom
        add(new JLabel("Nom:"));
        nomField = new JTextField();
        add(nomField);

        // Prénom
        add(new JLabel("Prénom:"));
        prenomField = new JTextField();
        add(prenomField);

        // Email
        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        // Mot de passe
        add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        add(passwordField);

        // Devise principale
        add(new JLabel("Devise principale:"));
        String[] devises = {"EUR", "USD", "GBP", "JPY"};
        deviseCombo = new JComboBox<>(devises);
        add(deviseCombo);

        // Objectif financier
        add(new JLabel("Objectif financier:"));
        objectifField = new JTextField();
        add(objectifField);

        // Bouton créer compte
        JButton registerButton = new JButton("Ajouter compte");
        add(new JLabel()); // placeholder
        add(registerButton);

        // Action bouton
        registerButton.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String devise = (String) deviseCombo.getSelectedItem();
            String objectif = objectifField.getText().trim();

            // Vérification des champs
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()
                    || devise == null || objectif.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tous les champs sont obligatoires.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validation de l'email
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer un email valide (exemple: utilisateur@domaine.com).",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validation du mot de passe
            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this,
                        "Le mot de passe doit contenir au moins 8 caractères.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.matches(".*[A-Z].*")) {
                JOptionPane.showMessageDialog(this,
                        "Le mot de passe doit contenir au moins une majuscule.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.matches(".*[0-9].*")) {
                JOptionPane.showMessageDialog(this,
                        "Le mot de passe doit contenir au moins un numéro.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Créer utilisateur
                Utilisateur u = new Utilisateur();
                u.setNom(nom);
                u.setPrenom(prenom);
                u.setEmail(email);
                u.setDateInscription(LocalDate.now());
                u.setDevisePrincipale(devise);
                u.setObjectifFinancier(objectif);

                // Hash du mot de passe
                u.setMotdepasse(BCrypt.hashpw(password, BCrypt.gensalt()));

                // Ajouter dans la base
                service.ajouter(u);

                JOptionPane.showMessageDialog(this,
                        "Compte créé avec succès !",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la création du compte : " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setLocationRelativeTo(parent); // centre sur la fenêtre parent
    }
}