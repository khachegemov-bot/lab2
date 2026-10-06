package ru.lab.cityreports.repository;

import ru.lab.cityreports.model.Identifiable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Базовое хранилище в памяти.
 * ConcurrentHashMap + AtomicLong — потокобезопасны: Tomcat обрабатывает запросы параллельно.
 */
public abstract class InMemoryRepository<T extends Identifiable> {

    private final Map<Long, T> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<T> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Identifiable::getId))
                .toList();
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    /** Если id нет — генерирует новый (id выдаёт только сервер). */
    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(sequence.incrementAndGet());
        }
        storage.put(entity.getId(), entity);
        return entity;
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    protected List<T> findWhere(Predicate<T> condition) {
        return storage.values().stream()
                .filter(condition)
                .sorted(Comparator.comparing(Identifiable::getId))
                .toList();
    }
}
