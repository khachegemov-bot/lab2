package ru.lab.cityreports.repository;

import org.springframework.stereotype.Repository;
import ru.lab.cityreports.model.CityReport;

import java.util.List;

@Repository
public class CityReportRepository extends InMemoryRepository<CityReport> {

    public List<CityReport> findByObjectId(Long objectId) {
        return findWhere(r -> r.getObjectId().equals(objectId));
    }

    public List<CityReport> findByAuthorId(Long authorId) {
        return findWhere(r -> r.getAuthorId().equals(authorId));
    }
}
