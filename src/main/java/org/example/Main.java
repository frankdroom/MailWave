package org.example;

import org.example.service.MailService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);


            MailService authenticatedService = loginDialog.getAuthenticatedMailService();

            if (authenticatedService != null) {
                MailClientFrame clientFrame = new MailClientFrame(authenticatedService);
                clientFrame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}