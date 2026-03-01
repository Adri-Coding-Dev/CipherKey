package dev.controller;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dev.model.PasswordEntry;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PasswordManager {
    private final Path vaultFile;
    private char[] masterPassword; // Solo se mantiene mientras la sesión está abierta
    private List<PasswordEntry> entries;
    private Gson gson;

    public PasswordManager(Path vaultFile) {
        this.vaultFile = vaultFile;
        this.gson = new Gson();
        this.entries = new ArrayList<>();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        });
        this.gson = gsonBuilder.create();
    }

    public boolean unlock(char[] password) {
        try {
            byte[] data = VaultManager.loadVault(vaultFile, password);
            String json = new String(data, StandardCharsets.UTF_8);
            Type listType = new TypeToken<ArrayList<PasswordEntry>>(){}.getType();
            List<PasswordEntry> loaded = gson.fromJson(json, listType);
            if (loaded == null) {
                loaded = new ArrayList<>();
            }
            this.entries = loaded;
            this.masterPassword = password.clone();
            return true;
        } catch (Exception e) {
            // Registrar la excepción para depuración
            e.printStackTrace();
            // Opcional: mostrar un mensaje más específico (solo durante desarrollo)
            // JOptionPane.showMessageDialog(null, "Error al abrir bóveda: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            Arrays.fill(password, (char) 0);
            return false;
        }
    }

    public void saveData() throws Exception {
        if (masterPassword == null) {
            throw new IllegalStateException("No hay sesión activa");
        }
        String json = gson.toJson(entries);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        VaultManager.saveVault(vaultFile, masterPassword, data);
    }

    public void lock() {
        if (masterPassword != null) {
            Arrays.fill(masterPassword, (char) 0);
            masterPassword = null;
        }
        entries.clear();
    }

    /**
     * Cambia la contraseña maestra de la bóveda.
     * @param oldPassword Contraseña actual (se limpiará después de usar)
     * @param newPassword Nueva contraseña (se limpiará después de usar)
     * @return true si el cambio fue exitoso
     */
    public boolean changeMasterPassword(char[] oldPassword, char[] newPassword) {
        try {
            // Verificar que la contraseña actual es correcta cargando los datos
            byte[] data = VaultManager.loadVault(vaultFile, oldPassword);
            // Guardar los mismos datos con la nueva contraseña
            VaultManager.saveVault(vaultFile, newPassword, data);
            // Si la sesión estaba abierta con la contraseña antigua, actualizar la copia en memoria
            if (masterPassword != null && Arrays.equals(masterPassword, oldPassword)) {
                Arrays.fill(masterPassword, (char) 0);
                masterPassword = newPassword.clone();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Métodos de gestión de entradas
    public List<PasswordEntry> getEntries() {
        return entries;
    }

    public void addEntry(PasswordEntry entry) {
        // Asignar un ID único basado en el máximo existente
        int newId = entries.stream().mapToInt(PasswordEntry::getId).max().orElse(0) + 1;
        entry.setId(newId);
        entries.add(entry);
    }

    public void updateEntry(PasswordEntry updated) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId() == updated.getId()) {
                entries.set(i, updated);
                return;
            }
        }
    }

    public void deleteEntry(int id) {
        entries.removeIf(e -> e.getId() == id);
    }
}