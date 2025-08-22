package app;

import model.Utilisateur;
import service.UtilisateurService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private UtilisateurService service;

    public LoginFrame() {
        service = new UtilisateurService();

        setTitle("Connexion");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 5, 5)); // 4 lignes pour email, mot de passe, boutons

        // Email
        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        // Mot de passe
        add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        add(passwordField);

        // Bouton se connecter
        JButton loginButton = new JButton("Se connecter");
        add(loginButton);

        // Bouton créer un compte
        JButton registerButton = new JButton("Créer un compte");
        add(registerButton);

        // Action connexion
        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String mdp = new String(passwordField.getPassword()).trim();

            if (service.verifierConnexion(email, mdp)) {
                Utilisateur utilisateurConnecte = service.getUtilisateurByEmail(email);
                JOptionPane.showMessageDialog(this, "Connexion réussie !");
                dispose();
                new Main(utilisateurConnecte).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Email ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action créer compte
        registerButton.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
        });

        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
