package ru.lab.cityreports.repository;

import org.springframework.stereotype.Repository;
import ru.lab.cityreports.model.WorkOrder;

import java.util.List;

@Repository
public class WorkOrderRepository extends InMemoryRepository<WorkOrder> {

    public List<WorkOrder> findByReportId(Long reportId) {
        return findWhere(o -> o.getReportId().equals(reportId));
    }

    public List<WorkOrder> findByCrewId(Long crewId) {
        return findWhere(o -> o.getCrewId().equals(crewId));
    }

    /** Количество активных (PLANNED / IN_PROGRESS) нарядов бригады. */
    public long countActiveByCrewId(Long crewId) {
        return findWhere(o -> o.getCrewId().equals(crewId) && o.getStatus().isActive()).size();
    }
}
