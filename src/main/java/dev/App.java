package dev;

import dev.controller.PasswordManager;
import dev.view.LoginDialog;
import dev.view.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.nio.file.Path;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            logger.warn("No se pudo establecer Nimbus L&F, usando el predeterminado.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                showLogin();
            } catch (Exception e) {
                logger.error("Error fatal en la aplicación", e);
                JOptionPane.showMessageDialog(null, "Error fatal: " + e.getMessage());
                System.exit(1);
            }
        });
    }

    public static void showLogin() {
        LoginDialog login = new LoginDialog(null);
        if (login.showDialog()) {
            char[] masterPwd = login.getPassword();
            Path vaultFile = login.getSelectedFile();
            PasswordManager manager = new PasswordManager(vaultFile);
            if (manager.unlock(masterPwd)) {
                login.clearPassword();
                new MainFrame(manager).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Contraseña incorrecta o archivo corrupto");
                showLogin();
            }
        } else {
            System.exit(0);
        }
    }
}