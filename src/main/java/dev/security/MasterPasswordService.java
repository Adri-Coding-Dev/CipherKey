package dev.security;

import dev.persistence.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class MasterPasswordService {
    private static final Logger logger = LoggerFactory.getLogger(MasterPasswordService.class);

    private final ConfigManager configManager;
    private byte[] salt;
    private byte[] storedHash; // hash de la contraseña maestra

    public MasterPasswordService() throws Exception {
        this.configManager = new ConfigManager();
        load();
    }

    private void load() throws Exception {
        salt = configManager.loadSalt();
        storedHash = configManager.loadHash();
    }

    public boolean isMasterPasswordSet() {
        return salt != null && storedHash != null;
    }

    /**
     * Crea una nueva contraseña maestra (primera configuración).
     */
    public void createMasterPassword(char[] password) throws Exception {
        if (isMasterPasswordSet()) {
            throw new IllegalStateException("La contraseña maestra ya existe");
        }
        byte[] newSalt = CryptoUtils.generateSalt();
        byte[] newHash = CryptoUtils.hashMasterPassword(password, newSalt);
        configManager.saveSaltAndHash(newSalt, newHash);
        this.salt = newSalt;
        this.storedHash = newHash;
    }

    /**
     * Verifica si la contraseña proporcionada es la correcta.
     */
    public boolean verifyMasterPassword(char[] password) throws Exception {
        if (!isMasterPasswordSet()) return false;
        byte[] hash = CryptoUtils.hashMasterPassword(password, salt);
        boolean match = Arrays.equals(hash, storedHash);
        Arrays.fill(hash, (byte) 0);
        return match;
    }

    /**
     * Cambia la contraseña maestra. Requiere la antigua y proporciona la nueva.
     * @param oldPassword contraseña actual (se limpiará después)
     * @param newPassword nueva contraseña (se limpiará después)
     * @return la nueva clave derivada (para recifrar la bóveda)
     */
    public SecretKey changeMasterPassword(char[] oldPassword, char[] newPassword) throws Exception {
        if (!verifyMasterPassword(oldPassword)) {
            throw new SecurityException("Contraseña antigua incorrecta");
        }
        // Generar nuevo salt y hash
        byte[] newSalt = CryptoUtils.generateSalt();
        byte[] newHash = CryptoUtils.hashMasterPassword(newPassword, newSalt);
        configManager.saveSaltAndHash(newSalt, newHash);
        this.salt = newSalt;
        this.storedHash = newHash;

        // Derivar nueva clave para la bóveda
        return CryptoUtils.deriveKey(newPassword, newSalt);
    }

    public byte[] getSalt() {
        return salt.clone(); // devolver copia para evitar modificaciones
    }
}
