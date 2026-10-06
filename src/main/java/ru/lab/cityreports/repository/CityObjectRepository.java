package ru.lab.cityreports.repository;

import org.springframework.stereotype.Repository;
import ru.lab.cityreports.model.CityObject;

@Repository
public class CityObjectRepository extends InMemoryRepository<CityObject> {
}
