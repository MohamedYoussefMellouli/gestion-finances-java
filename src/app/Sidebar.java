package app;

import javax.swing.*;
import java.awt.*;
import model.Utilisateur;

class Sidebar extends JPanel {
    private Utilisateur utilisateurConnecte;

    public Sidebar(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;

        setBackground(new Color(45, 45, 45));
        setPreferredSize(new Dimension(200, 0));
        setLayout(new GridLayout(4, 1, 0, 10)); // 4 boutons

        add(createButton("Modifier Compte", e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (parent instanceof Main main) {
                main.openModifierCompte();
            }
        }));

        add(createButton("Transactions", e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (parent instanceof Main main) {
                main.refreshTransactionTable();
            }
        }));

        add(createButton("Investissements", e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (parent instanceof Main main) {
                main.refreshInvestissementTable();
            }
        }));

        add(createButton("Déconnexion", e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (parent instanceof Main main) {
                main.seDeconnecter();
            }
        }));
    }

    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(listener);
        return btn;
    }
}
