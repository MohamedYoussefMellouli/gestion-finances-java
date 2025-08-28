package app;

import model.Utilisateur;
import service.UtilisateurService;
import utils.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import io.github.cdimascio.dotenv.Dotenv;

import com.twilio.http.NetworkHttpClient;
import com.twilio.http.HttpClient;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private UtilisateurService service;

    private static Map<String, String> otpStore = new HashMap<>();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String ACCOUNT_SID = dotenv.get("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = dotenv.get("TWILIO_AUTH_TOKEN");
    private static final String FROM_NUMBER = dotenv.get("TWILIO_FROM_NUMBER");

    public LoginFrame() {
        service = new UtilisateurService();

        setTitle("Connexion");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 5, 5));

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton("Se connecter");
        add(loginButton);

        JButton registerButton = new JButton("Créer un compte");
        add(registerButton);

        JButton forgotPasswordButton = new JButton("Mot de passe oublié ?");
        add(forgotPasswordButton);

        // Connexion
        loginButton.addActionListener(e -> connexion());

        // Créer compte
        registerButton.addActionListener(e -> new RegisterFrame(this).setVisible(true));

        // Mot de passe oublié
        forgotPasswordButton.addActionListener(e -> motDePasseOublie());

        setLocationRelativeTo(null);
    }

    private void connexion() {
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
    }

    private void motDePasseOublie() {
        String numero = JOptionPane.showInputDialog(this, "Entrez votre numéro de téléphone (+216...) :");

        if (numero != null && numero.matches("\\+216\\d{8}")) {
            String otp = generateOTP4();
            otpStore.put(numero, otp);

            if (!envoyerSms(numero, otp)) {
                JOptionPane.showMessageDialog(this, "Impossible d'envoyer le SMS. Vérifiez vos identifiants Twilio.");
                return;
            }

            String codeSaisi = JOptionPane.showInputDialog(this, "Entrez le code reçu par SMS :");
            if (codeSaisi != null && codeSaisi.equals(otp)) {
                String nouveauMdp = JOptionPane.showInputDialog(this, "Entrez votre nouveau mot de passe :");
                if (nouveauMdp != null && !nouveauMdp.trim().isEmpty()) {
                    if (mettreAJourMotDePasseParNumero(numero, nouveauMdp)) {
                        JOptionPane.showMessageDialog(this, "Mot de passe réinitialisé avec succès !");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du mot de passe !");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Code OTP incorrect !");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Numéro invalide. Format attendu : +216XXXXXXXX");
        }
    }

    // OTP 4 chiffres
    private String generateOTP4() {
        Random rand = new Random();
        int otp = 1000 + rand.nextInt(9000);
        return String.valueOf(otp);
    }

    private boolean envoyerSms(String numero, String otp) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(numero),
                    new com.twilio.type.PhoneNumber(FROM_NUMBER),
                    "Votre code de réinitialisation est : " + otp
            ).create();

            System.out.println("✅ SMS envoyé : " + message.getSid());
            return true; // ✅ tout s'est bien passé
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'envoi du SMS !");
            return false; // ❌ problème lors de l'envoi
        }
    }


    private boolean mettreAJourMotDePasseParNumero(String numero, String nouveauMdp) {
        String sql = "UPDATE utilisateur SET motdepasse = ? WHERE telephone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Hash du mot de passe avant mise à jour
            String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(nouveauMdp, org.mindrot.jbcrypt.BCrypt.gensalt());
            stmt.setString(1, hashed);
            stmt.setString(2, numero);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
