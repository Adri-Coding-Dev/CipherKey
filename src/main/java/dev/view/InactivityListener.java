package dev.view;

import javax.swing.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Listener que cierra la sesión tras un período de inactividad.
 */
public class InactivityListener implements AWTEventListener {
    private final int timeout; // milisegundos
    private Timer timer;
    private final Runnable onInactivity;

    public InactivityListener(int timeoutSeconds, Runnable onInactivity) {
        this.timeout = timeoutSeconds * 1000;
        this.onInactivity = onInactivity;
        resetTimer();
    }

    private void resetTimer() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(timeout, e -> {
            // Ejecutar acción de cierre de sesión en el hilo de eventos
            SwingUtilities.invokeLater(onInactivity);
        });
        timer.setRepeats(false);
        timer.start();
    }

    @Override
    public void eventDispatched(java.awt.AWTEvent event) {
        // Cualquier evento de teclado o ratón reinicia el timer
        if (event instanceof KeyEvent || event instanceof MouseEvent) {
            resetTimer();
        }
    }
}
