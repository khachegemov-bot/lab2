package ru.lab.cityreports.model;

import java.time.Instant;

/** Городской объект: адрес, тип объекта, состояние. */
public class CityObject implements Identifiable {
    private Long id;
    private String address;
    private ObjectType type;
    private ObjectState state;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public ObjectType getType() { return type; }
    public void setType(ObjectType type) { this.type = type; }
    public ObjectState getState() { return state; }
    public void setState(ObjectState state) { this.state = state; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
