package ru.lab.cityreports.service;

import org.springframework.stereotype.Service;
import ru.lab.cityreports.exception.BadRequestException;
import ru.lab.cityreports.exception.ConflictException;
import ru.lab.cityreports.exception.NotFoundException;
import ru.lab.cityreports.model.CityObject;
import ru.lab.cityreports.model.CityReport;
import ru.lab.cityreports.model.ReportStatus;
import ru.lab.cityreports.model.WorkCrew;
import ru.lab.cityreports.model.WorkOrder;
import ru.lab.cityreports.model.WorkOrderStatus;
import ru.lab.cityreports.repository.CityObjectRepository;
import ru.lab.cityreports.repository.CityReportRepository;
import ru.lab.cityreports.repository.WorkCrewRepository;
import ru.lab.cityreports.repository.WorkOrderRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WorkOrderService {

    /** Разрешённые переходы статусов наряда. DONE и CANCELLED — финальные. */
    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> TRANSITIONS = Map.of(
            WorkOrderStatus.PLANNED, Set.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.DONE, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.IN_PROGRESS, Set.of(WorkOrderStatus.DONE, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.DONE, Set.of(),
            WorkOrderStatus.CANCELLED, Set.of()
    );

    private final WorkOrderRepository workOrderRepository;
    private final CityReportRepository cityReportRepository;
    private final CityObjectRepository cityObjectRepository;
    private final WorkCrewRepository workCrewRepository;
    private final Clock clock;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            CityReportRepository cityReportRepository,
                            CityObjectRepository cityObjectRepository,
                            WorkCrewRepository workCrewRepository,
                            Clock clock) {
        this.workOrderRepository = workOrderRepository;
        this.cityReportRepository = cityReportRepository;
        this.cityObjectRepository = cityObjectRepository;
        this.workCrewRepository = workCrewRepository;
        this.clock = clock;
    }

    public List<WorkOrder> findAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder getById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Наряд с id=" + id + " не найден"));
    }

    public WorkOrder create(WorkOrder draft) {
        synchronized (DataLock.LOCK) {
            ensureDeadlineNotInPast(draft.getDeadline());

            CityReport report = cityReportRepository.findById(draft.getReportId())
                    .orElseThrow(() -> new NotFoundException("Обращение с id=" + draft.getReportId() + " не найдено"));
            WorkCrew crew = findCrew(draft.getCrewId());

            if (report.getStatus() == ReportStatus.RESOLVED || report.getStatus() == ReportStatus.REJECTED) {
                throw new ConflictException("Нельзя создать наряд по закрытому обращению (статус " + report.getStatus() + ")");
            }
            ensureCrewSupportsReport(crew, report);
            ensureCrewHasCapacity(crew);

            WorkOrder order = new WorkOrder();
            order.setReportId(report.getId());
            order.setCrewId(crew.getId());
            order.setDeadline(draft.getDeadline());
            order.setResultDescription(trimOrNull(draft.getResultDescription()));
            order.setStatus(WorkOrderStatus.PLANNED);     // статус задаёт сервер
            Instant now = Instant.now(clock);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            workOrderRepository.save(order);

            if (report.getStatus() == ReportStatus.NEW) {   // обращение взято в работу
                report.setStatus(ReportStatus.IN_PROGRESS);
                report.setUpdatedAt(now);
            }
            return order;
        }
    }

    public WorkOrder update(Long id, WorkOrder changes) {
        synchronized (DataLock.LOCK) {
            WorkOrder order = getById(id);
            WorkOrderStatus oldStatus = order.getStatus();
            WorkOrderStatus newStatus = changes.getStatus() != null ? changes.getStatus() : oldStatus;

            if (!oldStatus.isActive()) {
                throw new ConflictException("Наряд в статусе " + oldStatus + " закрыт и не может изменяться");
            }
            if (newStatus != oldStatus && !TRANSITIONS.get(oldStatus).contains(newStatus)) {
                throw new ConflictException("Недопустимый переход статуса: " + oldStatus + " -> " + newStatus);
            }
            CityReport report = cityReportRepository.findById(order.getReportId()).orElseThrow();

            // 1) СНАЧАЛА все проверки — чтобы при ошибке объект не остался изменённым наполовину
            boolean crewChanged = changes.getCrewId() != null && !changes.getCrewId().equals(order.getCrewId());
            if (crewChanged) {
                WorkCrew newCrew = findCrew(changes.getCrewId());
                ensureCrewSupportsReport(newCrew, report);
                if (newStatus.isActive()) {
                    ensureCrewHasCapacity(newCrew);
                }
            }
            if (changes.getDeadline() != null) {
                ensureDeadlineNotInPast(changes.getDeadline());
            }
            String newResult = changes.getResultDescription() != null
                    ? trimOrNull(changes.getResultDescription())
                    : order.getResultDescription();
            if (newStatus == WorkOrderStatus.DONE && newResult == null) {
                throw new BadRequestException("Для завершения наряда укажите resultDescription");
            }

            // 2) ПОТОМ применяем изменения
            Instant now = Instant.now(clock);
            if (crewChanged) {
                order.setCrewId(changes.getCrewId());
            }
            if (changes.getDeadline() != null) {
                order.setDeadline(changes.getDeadline());
            }
            order.setResultDescription(newResult);
            if (newStatus == WorkOrderStatus.DONE) {
                order.setCompletedAt(now);                  // время завершения — серверное
            }
            order.setStatus(newStatus);
            order.setUpdatedAt(now);
            workOrderRepository.save(order);

            if (newStatus == WorkOrderStatus.DONE) {
                report.setStatus(ReportStatus.RESOLVED);
                report.setUpdatedAt(now);
            } else if (newStatus == WorkOrderStatus.CANCELLED) {
                returnReportToNewIfIdle(report.getId());
            }
            return order;
        }
    }

    public void delete(Long id) {
        synchronized (DataLock.LOCK) {
            WorkOrder order = getById(id);
            workOrderRepository.deleteById(id);
            returnReportToNewIfIdle(order.getReportId());
        }
    }

    /** Вызывается при удалении обращения: удаляем наряды, которые на него ссылаются. */
    void deleteByReportId(Long reportId) {
        workOrderRepository.findByReportId(reportId).forEach(o -> workOrderRepository.deleteById(o.getId()));
    }

    /** Вызывается при удалении бригады: удаляем её наряды, обращения без активных нарядов возвращаем в NEW. */
    void deleteByCrewId(Long crewId) {
        for (WorkOrder order : workOrderRepository.findByCrewId(crewId)) {
            workOrderRepository.deleteById(order.getId());
            returnReportToNewIfIdle(order.getReportId());
        }
    }

    // ---------- вспомогательные проверки ----------

    private WorkCrew findCrew(Long crewId) {
        return workCrewRepository.findById(crewId)
                .orElseThrow(() -> new NotFoundException("Бригада с id=" + crewId + " не найдена"));
    }

    private void ensureDeadlineNotInPast(LocalDate deadline) {
        LocalDate today = LocalDate.now(clock);            // "сегодня" — по часам сервера
        if (deadline.isBefore(today)) {
            throw new BadRequestException("Срок (deadline) не может быть в прошлом. Сегодня: " + today);
        }
    }

    private void ensureCrewSupportsReport(WorkCrew crew, CityReport report) {
        CityObject object = cityObjectRepository.findById(report.getObjectId()).orElseThrow();
        if (!crew.getSupportedTypes().contains(object.getType())) {
            throw new ConflictException("Бригада '" + crew.getName() + "' не обслуживает объекты типа " + object.getType());
        }
    }

    private void ensureCrewHasCapacity(WorkCrew crew) {
        long active = workOrderRepository.countActiveByCrewId(crew.getId());
        if (active >= crew.getActiveWorkLimit()) {
            throw new ConflictException("Бригада '" + crew.getName() + "' достигла лимита активных работ ("
                    + crew.getActiveWorkLimit() + ")");
        }
    }

    /** Если у обращения не осталось активных нарядов и оно было "в работе" — возвращаем в NEW. */
    private void returnReportToNewIfIdle(Long reportId) {
        cityReportRepository.findById(reportId).ifPresent(report -> {
            boolean hasActive = workOrderRepository.findByReportId(reportId).stream()
                    .anyMatch(o -> o.getStatus().isActive());
            if (!hasActive && report.getStatus() == ReportStatus.IN_PROGRESS) {
                report.setStatus(ReportStatus.NEW);
                report.setUpdatedAt(Instant.now(clock));
            }
        });
    }

    private static String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
