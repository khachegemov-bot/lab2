package ru.lab.cityreports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Статус при создании не принимаем: новое обращение всегда NEW. */
public record CityReportCreateRequest(
        @NotNull @Positive Long objectId,
        @NotNull @Positive Long authorId,
        @NotBlank @Size(max = 2000) String description
) {}
