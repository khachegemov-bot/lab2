package ru.lab.cityreports.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lab.cityreports.dto.WorkCrewCreateRequest;
import ru.lab.cityreports.dto.WorkCrewPatchRequest;
import ru.lab.cityreports.dto.WorkCrewResponse;
import ru.lab.cityreports.model.WorkCrew;
import ru.lab.cityreports.service.WorkCrewService;

import java.net.URI;
import java.util.List;
import java.util.TreeSet;

@RestController
@RequestMapping("/api/work-crews")
public class WorkCrewController {

    private final WorkCrewService workCrewService;

    public WorkCrewController(WorkCrewService workCrewService) {
        this.workCrewService = workCrewService;
    }

    @GetMapping
    public List<WorkCrewResponse> getAll() {
        return workCrewService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public WorkCrewResponse getById(@PathVariable Long id) {
        return toResponse(workCrewService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkCrewResponse> create(@Valid @RequestBody WorkCrewCreateRequest request) {
        WorkCrew draft = new WorkCrew();
        draft.setName(request.name());
        draft.setSupportedTypes(request.supportedTypes());
        draft.setActiveWorkLimit(request.activeWorkLimit());
        WorkCrew created = workCrewService.create(draft);
        return ResponseEntity.created(URI.create("/api/work-crews/" + created.getId()))
                .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public WorkCrewResponse update(@PathVariable Long id, @Valid @RequestBody WorkCrewPatchRequest request) {
        WorkCrew changes = new WorkCrew();
        changes.setName(request.name());
        changes.setSupportedTypes(request.supportedTypes());
        changes.setActiveWorkLimit(request.activeWorkLimit());
        return toResponse(workCrewService.update(id, changes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workCrewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private WorkCrewResponse toResponse(WorkCrew c) {
        return new WorkCrewResponse(c.getId(), c.getName(), new TreeSet<>(c.getSupportedTypes()),
                c.getActiveWorkLimit(), workCrewService.countActiveWork(c.getId()),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
