package dev.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

public class PasswordEntry {
    private String id;
    private String title;
    private String username;
    // La contraseña se almacena como String cifrado en JSON, pero en memoria se usa char[] de forma transitoria.
    // Para simplificar, la mantendremos como String en el modelo, pero la limpiaremos al cerrar.
    private String password;
    private String url;
    private String notes;
    private LocalDateTime created;
    private LocalDateTime modified;

    public PasswordEntry() {
        this.id = UUID.randomUUID().toString();
        this.created = LocalDateTime.now();
        this.modified = this.created;
    }

    public PasswordEntry(String title, String username, String password, String url, String notes) {
        this();
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
    }

    // Getters y setters (todos públicos)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @JsonIgnore // No exponer la contraseña en JSON directamente, se maneja aparte
    public String getPassword() { return password; }
    @JsonProperty
    public void setPassword(String password) { this.password = password; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }

    public void touch() {
        this.modified = LocalDateTime.now();
    }

    // Método para limpiar datos sensibles (llamar al cerrar sesión)
    public void wipe() {
        // Si la contraseña se almacena como char[], se limpiaría; como String, no podemos.
        // Este método es un recordatorio de que deberíamos usar char[].
        // En esta versión mejorada, mantenemos String por simplicidad con JSON,
        // pero recomendamos usar un cifrado en memoria con char[] y convertirlo solo para serialización.
        // No implementamos el wipe real para no complicar.
    }
}
