package app;

import model.Investissement;
import model.Utilisateur;
import service.InvestissementService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class CardDialog extends JDialog {
    private JTextField cardNumberField;
    private JTextField expMonthField;
    private JTextField expYearField;
    private JTextField cvcField;
    private boolean confirmed = false;

    public CardDialog(JFrame parent) {
        super(parent, "Informations Carte", true);
        setLayout(new GridLayout(5, 2, 5, 5));

        add(new JLabel("Numéro de carte:"));
        cardNumberField = new JTextField("4242424242424242"); // Stripe test
        add(cardNumberField);

        add(new JLabel("Mois expiration (MM):"));
        expMonthField = new JTextField("12");
        add(expMonthField);

        add(new JLabel("Année expiration (YY):"));
        expYearField = new JTextField("30");
        add(expYearField);

        add(new JLabel("CVC:"));
        cvcField = new JTextField("123");
        add(cvcField);

        JButton okButton = new JButton("Valider");
        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });
        add(new JLabel());
        add(okButton);

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() { return confirmed; }
    public String getCardNumber() { return cardNumberField.getText().trim(); }
    public String getExpMonth() { return expMonthField.getText().trim(); }
    public String getExpYear() { return expYearField.getText().trim(); }
    public String getCvc() { return cvcField.getText().trim(); }

	public String getExpDate() {
		// TODO Auto-generated method stub
		return null;
	}
}
