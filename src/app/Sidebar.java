package app;

import javax.swing.*;

import model.Utilisateur;

import java.awt.*;
import java.awt.event.ActionListener;

class Sidebar extends JPanel {
    public Sidebar(ActionListener listener, Utilisateur utilisateurConnecte) {
        setBackground(new Color(45, 45, 45));
        setPreferredSize(new Dimension(200, 0));
        setLayout(new GridLayout(5, 1, 0, 10)); // maintenant 5 boutons

        add(createButton("Modifier Compte", listener));
        add(createTransactionButton(utilisateurConnecte)); // bouton Transactions
        add(createInvestissementButton(utilisateurConnecte)); // bouton Investissements
        add(createButton("Déconnexion", listener));
    }

    private JButton createTransactionButton(Utilisateur utilisateurConnecte) {
        JButton btn = new JButton("Transactions");
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            new ListeTransactionFrame(utilisateurConnecte).setVisible(true);
        });
        return btn;
    }

    private JButton createInvestissementButton(Utilisateur utilisateurConnecte) {
        JButton btn = new JButton("Investissements");
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            new ListeInvestissementFrame(utilisateurConnecte).setVisible(true);
        });
        return btn;
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(listener);
        return btn;
    }
}
