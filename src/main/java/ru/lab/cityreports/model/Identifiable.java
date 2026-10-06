package ru.lab.cityreports.model;

/** Общий интерфейс сущностей с id — нужен для универсального репозитория. */
public interface Identifiable {
    Long getId();
    void setId(Long id);
}
