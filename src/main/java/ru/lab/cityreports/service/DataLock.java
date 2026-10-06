package ru.lab.cityreports.service;

/**
 * Общая блокировка для всех операций записи.
 * Без неё два параллельных запроса могли бы одновременно пройти проверку лимита бригады
 * и оба создать наряд (race condition, TOCTOU — "проверил, а пока использовал, всё изменилось").
 */
final class DataLock {
    static final Object LOCK = new Object();

    private DataLock() {}
}
