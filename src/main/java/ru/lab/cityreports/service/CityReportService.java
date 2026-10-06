package ru.lab.cityreports.service;

import org.springframework.stereotype.Service;
import ru.lab.cityreports.exception.ConflictException;
import ru.lab.cityreports.exception.NotFoundException;
import ru.lab.cityreports.model.CityReport;
import ru.lab.cityreports.model.ReportStatus;
import ru.lab.cityreports.repository.CityObjectRepository;
import ru.lab.cityreports.repository.CityReportRepository;
import ru.lab.cityreports.repository.UserRepository;
import ru.lab.cityreports.repository.WorkOrderRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class CityReportService {

    private final CityReportRepository cityReportRepository;
    private final CityObjectRepository cityObjectRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderService workOrderService;
    private final Clock clock;

    public CityReportService(CityReportRepository cityReportRepository,
                             CityObjectRepository cityObjectRepository,
                             UserRepository userRepository,
                             WorkOrderRepository workOrderRepository,
                             WorkOrderService workOrderService,
                             Clock clock) {
        this.cityReportRepository = cityReportRepository;
        this.cityObjectRepository = cityObjectRepository;
        this.userRepository = userRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderService = workOrderService;
        this.clock = clock;
    }

    public List<CityReport> findAll() {
        return cityReportRepository.findAll();
    }

    public CityReport getById(Long id) {
        return cityReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Обращение с id=" + id + " не найдено"));
    }

    public CityReport create(CityReport draft) {
        synchronized (DataLock.LOCK) {
            // Проверяем ссылки: нельзя создать обращение на несуществующий объект/автора
            if (!cityObjectRepository.existsById(draft.getObjectId())) {
                throw new NotFoundException("Городской объект с id=" + draft.getObjectId() + " не найден");
            }
            if (!userRepository.existsById(draft.getAuthorId())) {
                throw new NotFoundException("Пользователь с id=" + draft.getAuthorId() + " не найден");
            }
            CityReport report = new CityReport();
            report.setObjectId(draft.getObjectId());
            report.setAuthorId(draft.getAuthorId());
            report.setDescription(draft.getDescription().trim());
            report.setStatus(ReportStatus.NEW);          // статус задаёт сервер
            Instant now = Instant.now(clock);
            report.setCreatedAt(now);
            report.setUpdatedAt(now);
            return cityReportRepository.save(report);
        }
    }

    public CityReport update(Long id, CityReport changes) {
        synchronized (DataLock.LOCK) {
            CityReport report = getById(id);
            // сначала проверки, потом изменения
            if (changes.getStatus() != null && changes.getStatus() != report.getStatus()) {
                boolean hasActiveOrders = workOrderRepository.findByReportId(id).stream()
                        .anyMatch(o -> o.getStatus().isActive());
                if (hasActiveOrders) {
                    throw new ConflictException(
                            "У обращения есть активные наряды — статус меняется через наряды (завершите или отмените их)");
                }
            }
            if (changes.getDescription() != null) {
                report.setDescription(changes.getDescription().trim());
            }
            if (changes.getStatus() != null) {
                report.setStatus(changes.getStatus());
            }
            report.setUpdatedAt(Instant.now(clock));
            return cityReportRepository.save(report);
        }
    }

    public void delete(Long id) {
        synchronized (DataLock.LOCK) {
            getById(id);
            deleteCascade(id);
        }
    }

    /** Вызывается при удалении объекта. */
    void deleteByObjectId(Long objectId) {
        cityReportRepository.findByObjectId(objectId).forEach(r -> deleteCascade(r.getId()));
    }

    /** Вызывается при удалении пользователя. */
    void deleteByAuthorId(Long authorId) {
        cityReportRepository.findByAuthorId(authorId).forEach(r -> deleteCascade(r.getId()));
    }

    private void deleteCascade(Long reportId) {
        workOrderService.deleteByReportId(reportId);   // сначала наряды, ссылающиеся на обращение
        cityReportRepository.deleteById(reportId);
    }
}
