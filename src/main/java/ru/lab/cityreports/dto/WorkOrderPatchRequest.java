package ru.lab.cityreports.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.lab.cityreports.model.WorkOrderStatus;

import java.time.LocalDate;

/** Обращение у наряда менять нельзя. Бригаду, срок, статус и результат — можно. */
public record WorkOrderPatchRequest(
        @Positive Long crewId,
        LocalDate deadline,
        WorkOrderStatus status,
        @Size(max = 2000) String resultDescription
) {}
