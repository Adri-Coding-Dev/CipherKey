package dev.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.model.PasswordEntry;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageManager {
    private static final String DATA_DIR = System.getProperty("user.home") + "/.passwordmanager/";
    private static final String VAULT_FILE = DATA_DIR + "vault.dat"; // cifrado
    private final ObjectMapper objectMapper;

    public StorageManager() throws IOException {
        Path dir = Paths.get(DATA_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            restrictDirectory(dir);
        }
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // No escribir fechas como marcas de tiempo
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void saveEncryptedData(byte[] encryptedData) throws IOException {
        Path path = Paths.get(VAULT_FILE);
        Files.write(path, encryptedData);
        restrictFile(path);
    }

    public byte[] loadEncryptedData() throws IOException {
        Path path = Paths.get(VAULT_FILE);
        return Files.exists(path) ? Files.readAllBytes(path) : null;
    }

    // Serializar lista de PasswordEntry a JSON (para cifrar)
    public byte[] serializeEntries(List<PasswordEntry> entries) throws IOException {
        return objectMapper.writeValueAsBytes(entries);
    }

    // Deserializar desde JSON
    public List<PasswordEntry> deserializeEntries(byte[] data) throws IOException {
        return objectMapper.readValue(data,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PasswordEntry.class));
    }

    private void restrictDirectory(Path dir) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(dir, perms);
        }
    }

    private void restrictFile(Path file) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file, perms);
        }
    }
}
