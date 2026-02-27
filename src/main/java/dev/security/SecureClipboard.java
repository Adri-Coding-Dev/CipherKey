package dev.security;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Gestor del portapapeles con limpieza automática después de un tiempo.
 */
public class SecureClipboard {
    private static final int CLEAR_DELAY = 30_000; // 30 segundos
    private static Timer timer;

    public static void copyToClipboard(String text) {
        // Cancelar temporizador anterior si existe
        if (timer != null) {
            timer.cancel();
        }

        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        // Programar limpieza
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Limpiar portapapeles (establecer cadena vacía)
                StringSelection empty = new StringSelection("");
                clipboard.setContents(empty, empty);
                timer = null;
            }
        }, CLEAR_DELAY);
    }
}