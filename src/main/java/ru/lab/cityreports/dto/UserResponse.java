package ru.lab.cityreports.dto;

import java.time.Instant;

public record UserResponse(Long id, String username, String fullName, String email,
                           Instant createdAt, Instant updatedAt) {}
