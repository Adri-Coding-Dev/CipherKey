package dev.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class LoginDialog extends JDialog {
    private JPasswordField passwordField;
    private boolean succeeded;
    private int attempts = 0;
    private long lastAttemptTime = 0;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME = 30000; // 30 segundos

    public LoginDialog(Frame parent) {
        super(parent, "Iniciar sesión", true);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Contraseña maestra:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        JButton okButton = new JButton("Aceptar");
        JButton cancelButton = new JButton("Cancelar");

        okButton.addActionListener(this::onOk);
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private void onOk(ActionEvent e) {
        // Protección contra fuerza bruta
        long now = System.currentTimeMillis();
        if (attempts >= MAX_ATTEMPTS && (now - lastAttemptTime) < LOCK_TIME) {
            long wait = (LOCK_TIME - (now - lastAttemptTime)) / 1000;
            JOptionPane.showMessageDialog(this,
                    "Demasiados intentos. Espere " + wait + " segundos.");
            return;
        }

        succeeded = true;
        dispose();
    }

    public boolean showDialog() {
        setVisible(true);
        return succeeded;
    }

    public char[] getPassword() {
        char[] pwd = passwordField.getPassword();
        attempts++;
        lastAttemptTime = System.currentTimeMillis();
        return pwd;
    }

    public void clearPassword() {
        Arrays.fill(passwordField.getPassword(), (char) 0);
        passwordField.setText("");
    }
}
