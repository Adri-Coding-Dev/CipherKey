package dev;

import dev.controller.PasswordManager;
import dev.security.MasterPasswordService;
import dev.view.LoginDialog;
import dev.view.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Configurar aspecto Nimbus (más moderno)
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            logger.warn("No se pudo establecer Nimbus L&F, usando el predeterminado.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MasterPasswordService mps = new MasterPasswordService();
                if (!mps.isMasterPasswordSet()) {
                    firstTimeSetup(mps);
                }
                showLogin();
            } catch (Exception e) {
                logger.error("Error fatal en la aplicación", e);
                JOptionPane.showMessageDialog(null, "Error fatal: " + e.getMessage());
                System.exit(1);
            }
        });
    }

    private static void firstTimeSetup(MasterPasswordService mps) throws Exception {
        // Diálogo simple para establecer la contraseña maestra
        JPasswordField pwdField1 = new JPasswordField(20);
        JPasswordField pwdField2 = new JPasswordField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Nueva contraseña maestra:"));
        panel.add(pwdField1);
        panel.add(new JLabel("Confirmar:"));
        panel.add(pwdField2);

        int result = JOptionPane.showConfirmDialog(null, panel, "Primera configuración",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            char[] pwd1 = pwdField1.getPassword();
            char[] pwd2 = pwdField2.getPassword();
            if (pwd1.length == 0) {
                JOptionPane.showMessageDialog(null, "La contraseña no puede estar vacía.");
                System.exit(0);
            }
            if (!java.util.Arrays.equals(pwd1, pwd2)) {
                JOptionPane.showMessageDialog(null, "Las contraseñas no coinciden.");
                System.exit(0);
            }
            mps.createMasterPassword(pwd1);
            // Limpiar memoria
            java.util.Arrays.fill(pwd1, (char) 0);
            java.util.Arrays.fill(pwd2, (char) 0);
            JOptionPane.showMessageDialog(null, "Contraseña maestra creada. Ahora puede iniciar sesión.");
        } else {
            System.exit(0);
        }
    }

    public static void showLogin() {
        LoginDialog login = new LoginDialog(null);
        if (login.showDialog()) {
            char[] masterPwd = login.getPassword();
            PasswordManager manager = new PasswordManager();
            if (manager.unlock(masterPwd)) {
                // Limpiar contraseña del diálogo
                login.clearPassword();
                new MainFrame(manager).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Contraseña incorrecta");
                showLogin(); // Reintentar (con protección de fuerza bruta en LoginDialog)
            }
        } else {
            System.exit(0);
        }
    }
}
