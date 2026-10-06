package ru.lab.cityreports.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.lab.cityreports.model.ReportStatus;

/** Объект и автора у обращения менять нельзя — только описание и статус. */
public record CityReportPatchRequest(
        @Size(max = 2000) @Pattern(regexp = "(?s).*\\S.*", message = "не должно быть пустым") String description,
        ReportStatus status
) {}
