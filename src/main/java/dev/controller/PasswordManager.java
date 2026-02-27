package dev.controller;

import dev.model.PasswordEntry;
import dev.persistence.StorageManager;
import dev.security.CryptoUtils;
import dev.security.MasterPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

public class PasswordManager {
    private static final Logger logger = LoggerFactory.getLogger(PasswordManager.class);

    private List<PasswordEntry> entries;
    private MasterPasswordService masterService;
    private StorageManager storage;
    private SecretKey vaultKey;          // Clave derivada de la contraseña maestra (solo cuando está desbloqueado)
    private boolean unlocked;

    public PasswordManager() {
        try {
            this.masterService = new MasterPasswordService();
            this.storage = new StorageManager();
            this.entries = new ArrayList<>();
            this.unlocked = false;
        } catch (Exception e) {
            logger.error("Error al inicializar PasswordManager", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Intenta desbloquear la bóveda con la contraseña maestra.
     * @param masterPassword Contraseña maestra en char[] (se limpiará después)
     * @return true si es correcta y se cargaron los datos
     */
    public boolean unlock(char[] masterPassword) {
        try {
            if (!masterService.verifyMasterPassword(masterPassword)) {
                return false;
            }
            // Derivar clave para cifrar/descifrar la bóveda
            byte[] salt = masterService.getSalt();
            this.vaultKey = CryptoUtils.deriveKey(masterPassword, salt); // masterPassword se limpia dentro de deriveKey

            // Cargar datos cifrados
            byte[] encryptedData = storage.loadEncryptedData();
            if (encryptedData != null && encryptedData.length > 0) {
                byte[] plainData = CryptoUtils.decrypt(encryptedData, vaultKey);
                entries = storage.deserializeEntries(plainData);
            } else {
                entries = new ArrayList<>(); // Bóveda nueva
            }
            unlocked = true;
            return true;
        } catch (Exception e) {
            logger.error("Error al desbloquear", e);
            return false;
        }
    }

    public void lock() {
        // Limpiar datos sensibles
        vaultKey = null;
        entries.clear();
        unlocked = false;
        System.gc(); // Sugerir recolección
    }

    public boolean isUnlocked() { return unlocked; }

    public List<PasswordEntry> getEntries() { return entries; }

    public void addEntry(PasswordEntry entry) {
        entry.touch();
        entries.add(entry);
    }

    public void updateEntry(PasswordEntry entry) {
        entry.touch();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(entry.getId())) {
                entries.set(i, entry);
                break;
            }
        }
    }

    public void deleteEntry(String id) {
        entries.removeIf(e -> e.getId().equals(id));
    }

    /**
     * Guarda los datos actuales cifrados en disco.
     */
    public void saveData() throws Exception {
        if (!unlocked || vaultKey == null) {
            throw new IllegalStateException("Bóveda no desbloqueada");
        }
        byte[] plainData = storage.serializeEntries(entries);
        byte[] encrypted = CryptoUtils.encrypt(plainData, vaultKey);
        storage.saveEncryptedData(encrypted);
    }

    /**
     * Cambia la contraseña maestra y recifra la bóveda con la nueva clave.
     */
    public boolean changeMasterPassword(char[] oldPassword, char[] newPassword) {
        try {
            // Verificar antigua y obtener nueva clave
            SecretKey newKey = masterService.changeMasterPassword(oldPassword, newPassword);
            // Recifrar todos los datos con la nueva clave
            byte[] plainData = storage.serializeEntries(entries);
            byte[] encrypted = CryptoUtils.encrypt(plainData, newKey);
            storage.saveEncryptedData(encrypted);
            // Actualizar clave en memoria
            this.vaultKey = newKey;
            return true;
        } catch (Exception e) {
            logger.error("Error al cambiar contraseña maestra", e);
            return false;
        }
    }
}
