package ru.lab.cityreports.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lab.cityreports.dto.UserCreateRequest;
import ru.lab.cityreports.dto.UserPatchRequest;
import ru.lab.cityreports.dto.UserResponse;
import ru.lab.cityreports.model.User;
import ru.lab.cityreports.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {                                   // 200
        return userService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {                  // 200
        return toResponse(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User draft = new User();
        draft.setUsername(request.username());
        draft.setFullName(request.fullName());
        draft.setEmail(request.email());
        User created = userService.create(draft);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(toResponse(created));                              // 201
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserPatchRequest request) {
        User changes = new User();
        changes.setUsername(request.username());
        changes.setFullName(request.fullName());
        changes.setEmail(request.email());
        return toResponse(userService.update(id, changes));               // 200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();                        // 204
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(),
                u.getCreatedAt(), u.getUpdatedAt());
    }
}
