package dev.controller;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class VaultManager {
    private static final int ITERATIONS = 300_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 32;
    private static final int NONCE_LENGTH = 12;
    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";
    private static final String AES_ALGO = "AES/GCM/NoPadding";

    public static void createVault(Path file, char[] masterPassword) throws Exception {
        if (Files.exists(file)) {
            throw new IllegalArgumentException("El archivo ya existe: " + file);
        }

        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        SecretKey key = deriveKey(masterPassword, salt);
        byte[] emptyData = "[]".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(emptyData, key);

        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            fos.write(salt);
            fos.write(encrypted);
        }
    }

    public static byte[] loadVault(Path file, char[] masterPassword) throws Exception {
        if (!Files.exists(file)) {
            throw new FileNotFoundException("El archivo no existe: " + file);
        }

        byte[] fileContent = Files.readAllBytes(file);
        if (fileContent.length < SALT_LENGTH + NONCE_LENGTH) {
            throw new IOException("Archivo de bóveda corrupto (tamaño insuficiente)");
        }

        byte[] salt = Arrays.copyOfRange(fileContent, 0, SALT_LENGTH);
        SecretKey key = deriveKey(masterPassword, salt);
        byte[] encryptedData = Arrays.copyOfRange(fileContent, SALT_LENGTH, fileContent.length);
        return decrypt(encryptedData, key);
    }

    public static void saveVault(Path file, char[] masterPassword, byte[] data) throws Exception {
        if (!Files.exists(file)) {
            throw new FileNotFoundException("El archivo no existe: " + file);
        }

        byte[] fileContent = Files.readAllBytes(file);
        if (fileContent.length < SALT_LENGTH) {
            throw new IOException("Archivo de bóveda corrupto (sin salt)");
        }
        byte[] salt = Arrays.copyOfRange(fileContent, 0, SALT_LENGTH);

        SecretKey key = deriveKey(masterPassword, salt);
        byte[] encrypted = encrypt(data, key);

        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            fos.write(salt);
            fos.write(encrypted);
        }
    }

    /**
     * Verifica si una bóveda puede abrirse con la contraseña dada.
     */
    public static boolean testVault(Path file, char[] password) {
        try {
            loadVault(file, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Métodos privados (deriveKey, encrypt, decrypt) sin cambios
    private static SecretKey deriveKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGO);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGO);
        byte[] nonce = new byte[NONCE_LENGTH];
        new SecureRandom().nextBytes(nonce);
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] nonceAndCiphertext = new byte[NONCE_LENGTH + ciphertext.length];
        System.arraycopy(nonce, 0, nonceAndCiphertext, 0, NONCE_LENGTH);
        System.arraycopy(ciphertext, 0, nonceAndCiphertext, NONCE_LENGTH, ciphertext.length);
        return nonceAndCiphertext;
    }

    private static byte[] decrypt(byte[] nonceAndCiphertext, SecretKey key) throws Exception {
        if (nonceAndCiphertext.length < NONCE_LENGTH) {
            throw new IllegalArgumentException("Datos cifrados demasiado cortos");
        }
        byte[] nonce = Arrays.copyOfRange(nonceAndCiphertext, 0, NONCE_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(nonceAndCiphertext, NONCE_LENGTH, nonceAndCiphertext.length);
        Cipher cipher = Cipher.getInstance(AES_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(ciphertext);
    }
}