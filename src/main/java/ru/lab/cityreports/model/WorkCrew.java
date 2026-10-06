package ru.lab.cityreports.model;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/** Рабочая бригада: название, поддерживаемые типы объектов, лимит активных работ. */
public class WorkCrew implements Identifiable {
    private Long id;
    private String name;
    private Set<ObjectType> supportedTypes;
    private Integer activeWorkLimit;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<ObjectType> getSupportedTypes() { return supportedTypes; }
    public void setSupportedTypes(Set<ObjectType> supportedTypes) {
        // храним копию, чтобы внешний код не мог изменить набор "в обход" сервиса
        if (supportedTypes == null) {
            this.supportedTypes = null;
        } else if (supportedTypes.isEmpty()) {
            this.supportedTypes = EnumSet.noneOf(ObjectType.class);
        } else {
            this.supportedTypes = EnumSet.copyOf(supportedTypes);
        }
    }
    public Integer getActiveWorkLimit() { return activeWorkLimit; }
    public void setActiveWorkLimit(Integer activeWorkLimit) { this.activeWorkLimit = activeWorkLimit; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
