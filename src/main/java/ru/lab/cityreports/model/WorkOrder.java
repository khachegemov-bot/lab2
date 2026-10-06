package ru.lab.cityreports.model;

import java.time.Instant;
import java.time.LocalDate;

/** Наряд на работу: обращение, бригада, срок, статус, описание результата. */
public class WorkOrder implements Identifiable {
    private Long id;
    private Long reportId;     // ссылка на CityReport
    private Long crewId;       // ссылка на WorkCrew
    private LocalDate deadline;
    private WorkOrderStatus status;
    private String resultDescription;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;   // ставится сервером при переходе в DONE

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Long getCrewId() { return crewId; }
    public void setCrewId(Long crewId) { this.crewId = crewId; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
    public String getResultDescription() { return resultDescription; }
    public void setResultDescription(String resultDescription) { this.resultDescription = resultDescription; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
