package ru.lab.cityreports.service;

import org.springframework.stereotype.Service;
import ru.lab.cityreports.exception.ConflictException;
import ru.lab.cityreports.exception.NotFoundException;
import ru.lab.cityreports.model.User;
import ru.lab.cityreports.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CityReportService cityReportService;
    private final Clock clock;

    public UserService(UserRepository userRepository, CityReportService cityReportService, Clock clock) {
        this.userRepository = userRepository;
        this.cityReportService = cityReportService;
        this.clock = clock;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User create(User draft) {
        synchronized (DataLock.LOCK) {
            String username = draft.getUsername().trim();
            ensureUsernameFree(username, null);

            User user = new User();                      // id и время ставит только сервер
            user.setUsername(username);
            user.setFullName(draft.getFullName().trim());
            user.setEmail(draft.getEmail().trim().toLowerCase(Locale.ROOT));
            Instant now = Instant.now(clock);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            return userRepository.save(user);
        }
    }

    public User update(Long id, User changes) {
        synchronized (DataLock.LOCK) {
            User user = getById(id);
            if (changes.getUsername() != null) {
                String username = changes.getUsername().trim();
                ensureUsernameFree(username, id);
                user.setUsername(username);
            }
            if (changes.getFullName() != null) {
                user.setFullName(changes.getFullName().trim());
            }
            if (changes.getEmail() != null) {
                user.setEmail(changes.getEmail().trim().toLowerCase(Locale.ROOT));
            }
            user.setUpdatedAt(Instant.now(clock));
            return userRepository.save(user);
        }
    }

    /** Каскад: удаляем обращения пользователя (а они — свои наряды). Висячих authorId не остаётся. */
    public void delete(Long id) {
        synchronized (DataLock.LOCK) {
            getById(id);
            cityReportService.deleteByAuthorId(id);
            userRepository.deleteById(id);
        }
    }

    private void ensureUsernameFree(String username, Long currentUserId) {
        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ConflictException("Имя пользователя '" + username + "' уже занято");
                });
    }
}
