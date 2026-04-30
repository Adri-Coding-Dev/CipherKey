package dev.utils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.util.Properties;

public class EnviarCorreo {

    private static String generatedCode;
    private static long expirationTime;

    private static final String from = "<Direccion de email que va a enviar el correo>";
    private static final String password = "<clave de aplicacion de google>";

    public EnviarCorreo(String userEmail){
        enviarCodigo(userEmail);
    }

    public EnviarCorreo(){}

    // 🔹 Método público: enviar código
    public static void enviarCodigo(String toEmail) {
        generatedCode = generarCodigo();
        expirationTime = System.currentTimeMillis() + (5 * 60 * 1000);

        enviarEmail(toEmail, generatedCode);
    }

    // 🔹 Método público: verificar código
    public boolean verificarCodigo(String inputCode) {
        if (System.currentTimeMillis() > expirationTime) {
            return false;
        }
        return inputCode.equals(generatedCode);
    }

    // 🔐 Generar OTP
    private static String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 📧 Envío de email
    private static void enviarEmail(String to, String code) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Código de verificación");

            message.setContent(buildTemplate(code), "text/html; charset=utf-8");

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando email", e);
        }
    }

    // 🎨 Template HTML
    private static String buildTemplate(String code) {
        return """
        <!DOCTYPE html>
        <html>
        <body style="margin:0;padding:0;font-family:Arial;background:#f4f4f4;">
            <table width="100%%">
                <tr>
                    <td align="center">
                        <table width="400" style="background:white;padding:20px;border-radius:10px;">
                            <tr>
                                <td align="center">
                                    <h2>Verificación</h2>
                                    <p>Tu código es:</p>
                                    <div style="font-size:30px;font-weight:bold;color:#2d89ef;">
                                        %s
                                    </div>
                                    <p style="font-size:12px;color:gray;">
                                        Expira en 5 minutos
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(code);
    }
}
