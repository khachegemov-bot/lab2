package ru.lab.cityreports.model;

/** Статус наряда на работу. PLANNED и IN_PROGRESS считаются "активными". */
public enum WorkOrderStatus {
    PLANNED, IN_PROGRESS, DONE, CANCELLED;

    public boolean isActive() {
        return this == PLANNED || this == IN_PROGRESS;
    }
}
