package ru.lab.cityreports.service;

import org.springframework.stereotype.Service;
import ru.lab.cityreports.exception.ConflictException;
import ru.lab.cityreports.exception.NotFoundException;
import ru.lab.cityreports.model.CityObject;
import ru.lab.cityreports.model.CityReport;
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

@Service
public class CityObjectService {

    private final CityObjectRepository cityObjectRepository;
    private final CityReportRepository cityReportRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkCrewRepository workCrewRepository;
    private final CityReportService cityReportService;
    private final Clock clock;

    public CityObjectService(CityObjectRepository cityObjectRepository,
                             CityReportRepository cityReportRepository,
                             WorkOrderRepository workOrderRepository,
                             WorkCrewRepository workCrewRepository,
                             CityReportService cityReportService,
                             Clock clock) {
        this.cityObjectRepository = cityObjectRepository;
        this.cityReportRepository = cityReportRepository;
        this.workOrderRepository = workOrderRepository;
        this.workCrewRepository = workCrewRepository;
        this.cityReportService = cityReportService;
        this.clock = clock;
    }

    public List<CityObject> findAll() {
        return cityObjectRepository.findAll();
    }

    public CityObject getById(Long id) {
        return cityObjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Городской объект с id=" + id + " не найден"));
    }

    public CityObject create(CityObject draft) {
        synchronized (DataLock.LOCK) {
            CityObject object = new CityObject();
            object.setAddress(draft.getAddress().trim());
            object.setType(draft.getType());
            object.setState(draft.getState());
            Instant now = Instant.now(clock);
            object.setCreatedAt(now);
            object.setUpdatedAt(now);
            return cityObjectRepository.save(object);
        }
    }

    public CityObject update(Long id, CityObject changes) {
        synchronized (DataLock.LOCK) {
            CityObject object = getById(id);
            // сначала проверки, потом изменения
            if (changes.getType() != null && changes.getType() != object.getType()) {
                ensureActiveCrewsSupport(id, changes.getType());
            }
            if (changes.getAddress() != null) {
                object.setAddress(changes.getAddress().trim());
            }
            if (changes.getType() != null) {
                object.setType(changes.getType());
            }
            if (changes.getState() != null) {
                object.setState(changes.getState());
            }
            object.setUpdatedAt(Instant.now(clock));
            return cityObjectRepository.save(object);
        }
    }

    /** Каскад: удаляем обращения по объекту (и их наряды). */
    public void delete(Long id) {
        synchronized (DataLock.LOCK) {
            getById(id);
            cityReportService.deleteByObjectId(id);
            cityObjectRepository.deleteById(id);
        }
    }

    /** Нельзя сменить тип объекта, если над ним активно работает бригада, которая этот тип не поддерживает. */
    private void ensureActiveCrewsSupport(Long objectId, ObjectType newType) {
        for (CityReport report : cityReportRepository.findByObjectId(objectId)) {
            for (WorkOrder order : workOrderRepository.findByReportId(report.getId())) {
                if (!order.getStatus().isActive()) {
                    continue;
                }
                WorkCrew crew = workCrewRepository.findById(order.getCrewId()).orElseThrow();
                if (!crew.getSupportedTypes().contains(newType)) {
                    throw new ConflictException("Нельзя сменить тип объекта: активный наряд id=" + order.getId()
                            + " выполняет бригада, не поддерживающая тип " + newType);
                }
            }
        }
    }
}
