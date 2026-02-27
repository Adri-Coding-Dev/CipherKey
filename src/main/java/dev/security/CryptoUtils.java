package dev.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class CryptoUtils {
    static {
        // Añadir Bouncy Castle como proveedor de seguridad
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int ITERATIONS = 300_000; // Aumentado para 2025
    private static final int KEY_LENGTH = 256;
    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Deriva una clave AES a partir de una contraseña (char[]) y un salt.
     * IMPORTANTE: La contraseña se limpia del array después de usarla.
     */
    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGO, "BC");
            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } finally {
            Arrays.fill(password, (char) 0); // Limpiar contraseña de memoria
        }
    }

    /**
     * Versión con String (menos segura, usar sólo cuando sea inevitable).
     */
    public static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        return deriveKey(password.toCharArray(), salt);
    }

    /**
     * Cifra datos con AES-GCM. El resultado incluye el IV al inicio.
     */
    public static byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Combinar IV + ciphertext
        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
        return encrypted;
    }

    /**
     * Descifra datos cifrados con encrypt().
     */
    public static byte[] decrypt(byte[] encrypted, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[encrypted.length - GCM_IV_LENGTH];
        System.arraycopy(encrypted, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(encrypted, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return cipher.doFinal(ciphertext);
    }

    /**
     * Genera un salt aleatorio de 16 bytes.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Calcula el hash de la contraseña maestra (PBKDF2) para almacenamiento.
     * Devuelve el hash en bytes (no en Base64 para almacenar junto con salt).
     */
    public static byte[] hashMasterPassword(char[] password, byte[] salt) throws Exception {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGO, "BC");
            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, 256);
            SecretKey tmp = factory.generateSecret(spec);
            return tmp.getEncoded();
        } finally {
            Arrays.fill(password, (char) 0);
        }
    }
}
