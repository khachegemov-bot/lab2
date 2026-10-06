package ru.lab.cityreports.repository;

import org.springframework.stereotype.Repository;
import ru.lab.cityreports.model.User;

import java.util.Optional;

@Repository
public class UserRepository extends InMemoryRepository<User> {

    public Optional<User> findByUsername(String username) {
        return findWhere(u -> u.getUsername().equalsIgnoreCase(username)).stream().findFirst();
    }
}
