package ru.lab.cityreports.model;

import java.time.Instant;

/** Пользователь системы (житель, который подаёт обращения). */
public class User implements Identifiable {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Instant createdAt;   // задаётся сервером
    private Instant updatedAt;   // задаётся сервером

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
