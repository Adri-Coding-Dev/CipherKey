package dev.view;

import dev.controller.PasswordManager;
import dev.persistence.ConfigManager;
import dev.utils.EnviarCorreo;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class EmailVerificatorView {

    private JFrame frame;
    private JTextField inputField;  // Se usará para el código
    private JButton button;
    private JLabel label;

    private EnviarCorreo servicio = new EnviarCorreo();
    private boolean modoVerificacion = false;
    private String userEmail;   // Correo cargado de la configuración

    private Path vaultFile;
    private PasswordManager manager;

    public EmailVerificatorView(PasswordManager manager, Path vaultFileApp) {
        this.manager = manager;
        this.vaultFile = vaultFileApp;
        // Cargar el correo guardado
        try {
            userEmail = new ConfigManager().loadEmail();
        } catch (Exception e) {
            userEmail = null;
        }

        if (userEmail == null || userEmail.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontró un correo de verificación asociado a esta bóveda.");
            // Si no hay email, no se puede continuar; cerrar o salir.
            System.exit(1);
        }

        frame = new JFrame("Verificación Email");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4, 1));  // Ahora 4 filas (label + campo + botón + espacio)

        label = new JLabel("Enviar código de verificación a: " + userEmail, SwingConstants.CENTER);
        button = new JButton("Enviar código");

        inputField = new JTextField();
        inputField.setVisible(false);  // Inicialmente oculto, se muestra al cambiar a modo verificación

        button.addActionListener(e -> manejarAccion());

        frame.add(label);
        frame.add(inputField);
        frame.add(button);

        frame.setVisible(true);
    }

    private void manejarAccion() {
        if (!modoVerificacion) {
            //Enviar código al email cargado
            try {
                EnviarCorreo.enviarCodigo(userEmail);
                cambiarAVerificacion();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error enviando correo: " + ex.getMessage());
            }
        } else {
            // Verificar código ingresado
            String codigo = inputField.getText().trim();
            if (servicio.verificarCodigo(codigo)) {
                frame.dispose();
                SwingUtilities.invokeLater(() -> {
                    new MainFrame(manager).setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(frame, "Código incorrecto o expirado ❌");
            }
        }
    }

    private void cambiarAVerificacion() {
        modoVerificacion = true;
        label.setText("Introduce el código recibido:");
        inputField.setVisible(true);
        inputField.setText("");
        button.setText("Verificar");
        frame.revalidate();
        frame.repaint();
    }
}