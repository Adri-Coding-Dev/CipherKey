package dev.persistence;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.passwordmanager/";
    private static final String SALT_FILE = CONFIG_DIR + "salt.dat";
    private static final String HASH_FILE = CONFIG_DIR + "hash.dat";

    public ConfigManager() throws IOException {
        Path dir = Paths.get(CONFIG_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            // Restringir permisos en sistemas Unix (rwx------)
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                Files.setPosixFilePermissions(dir, perms);
            }
        }
    }

    public void saveSaltAndHash(byte[] salt, byte[] hash) throws IOException {
        Files.write(Paths.get(SALT_FILE), salt);
        Files.write(Paths.get(HASH_FILE), hash);
        // Restringir permisos de los archivos
        restrictPermissions(Paths.get(SALT_FILE));
        restrictPermissions(Paths.get(HASH_FILE));
    }

    public byte[] loadSalt() throws IOException {
        Path path = Paths.get(SALT_FILE);
        return Files.exists(path) ? Files.readAllBytes(path) : null;
    }

    public byte[] loadHash() throws IOException {
        Path path = Paths.get(HASH_FILE);
        return Files.exists(path) ? Files.readAllBytes(path) : null;
    }

    private void restrictPermissions(Path path) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(path, perms);
        }
    }
}
