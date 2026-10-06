package ru.lab.cityreports.service;

import org.springframework.stereotype.Service;
import ru.lab.cityreports.exception.ConflictException;
import ru.lab.cityreports.exception.NotFoundException;
import ru.lab.cityreports.model.ObjectType;
import ru.lab.cityreports.model.WorkCrew;
import ru.lab.cityreports.model.WorkOrder;
import ru.lab.cityreports.repository.CityObjectRepository;
import ru.lab.cityreports.repository.CityReportRepository;
import ru.lab.cityreports.repository.WorkCrewRepository;
import ru.lab.cityreports.repository.WorkOrderRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class WorkCrewService {

    private final WorkCrewRepository workCrewRepository;
    private final WorkOrderRepository workOrderRepository;
    private final CityReportRepository cityReportRepository;
    private final CityObjectRepository cityObjectRepository;
    private final WorkOrderService workOrderService;
    private final Clock clock;

    public WorkCrewService(WorkCrewRepository workCrewRepository,
                           WorkOrderRepository workOrderRepository,
                           CityReportRepository cityReportRepository,
                           CityObjectRepository cityObjectRepository,
                           WorkOrderService workOrderService,
                           Clock clock) {
        this.workCrewRepository = workCrewRepository;
        this.workOrderRepository = workOrderRepository;
        this.cityReportRepository = cityReportRepository;
        this.cityObjectRepository = cityObjectRepository;
        this.workOrderService = workOrderService;
        this.clock = clock;
    }

    public List<WorkCrew> findAll() {
        return workCrewRepository.findAll();
    }

    public WorkCrew getById(Long id) {
        return workCrewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бригада с id=" + id + " не найдена"));
    }

    /** Сколько активных нарядов сейчас у бригады (вычисляемое поле для ответа). */
    public long countActiveWork(Long crewId) {
        return workOrderRepository.countActiveByCrewId(crewId);
    }

    public WorkCrew create(WorkCrew draft) {
        synchronized (DataLock.LOCK) {
            WorkCrew crew = new WorkCrew();
            crew.setName(draft.getName().trim());
            crew.setSupportedTypes(draft.getSupportedTypes());
            crew.setActiveWorkLimit(draft.getActiveWorkLimit());
            Instant now = Instant.now(clock);
            crew.setCreatedAt(now);
            crew.setUpdatedAt(now);
            return workCrewRepository.save(crew);
        }
    }

    public WorkCrew update(Long id, WorkCrew changes) {
        synchronized (DataLock.LOCK) {
            WorkCrew crew = getById(id);
            // сначала проверки, потом изменения
            if (changes.getActiveWorkLimit() != null) {
                long active = workOrderRepository.countActiveByCrewId(id);
                if (changes.getActiveWorkLimit() < active) {
                    throw new ConflictException("Новый лимит (" + changes.getActiveWorkLimit()
                            + ") меньше текущего числа активных работ (" + active + ")");
                }
            }
            if (changes.getSupportedTypes() != null) {
                ensureActiveOrdersStillSupported(id, changes.getSupportedTypes());
            }
            if (changes.getName() != null) {
                crew.setName(changes.getName().trim());
            }
            if (changes.getActiveWorkLimit() != null) {
                crew.setActiveWorkLimit(changes.getActiveWorkLimit());
            }
            if (changes.getSupportedTypes() != null) {
                crew.setSupportedTypes(changes.getSupportedTypes());
            }
            crew.setUpdatedAt(Instant.now(clock));
            return workCrewRepository.save(crew);
        }
    }

    /** Каскад: удаляем наряды бригады. */
    public void delete(Long id) {
        synchronized (DataLock.LOCK) {
            getById(id);
            workOrderService.deleteByCrewId(id);
            workCrewRepository.deleteById(id);
        }
    }

    /** Нельзя убрать тип, по которому у бригады есть активный наряд. */
    private void ensureActiveOrdersStillSupported(Long crewId, Set<ObjectType> newTypes) {
        for (WorkOrder order : workOrderRepository.findByCrewId(crewId)) {
            if (!order.getStatus().isActive()) {
                continue;
            }
            Long objectId = cityReportRepository.findById(order.getReportId()).orElseThrow().getObjectId();
            ObjectType type = cityObjectRepository.findById(objectId).orElseThrow().getType();
            if (!newTypes.contains(type)) {
                throw new ConflictException("Нельзя убрать тип " + type
                        + ": у бригады есть активный наряд id=" + order.getId() + " по объекту этого типа");
            }
        }
    }
}
