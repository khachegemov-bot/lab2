package ru.lab.cityreports.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lab.cityreports.dto.CityReportCreateRequest;
import ru.lab.cityreports.dto.CityReportPatchRequest;
import ru.lab.cityreports.dto.CityReportResponse;
import ru.lab.cityreports.model.CityReport;
import ru.lab.cityreports.service.CityReportService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/city-reports")
public class CityReportController {

    private final CityReportService cityReportService;

    public CityReportController(CityReportService cityReportService) {
        this.cityReportService = cityReportService;
    }

    @GetMapping
    public List<CityReportResponse> getAll() {
        return cityReportService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CityReportResponse getById(@PathVariable Long id) {
        return toResponse(cityReportService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CityReportResponse> create(@Valid @RequestBody CityReportCreateRequest request) {
        CityReport draft = new CityReport();
        draft.setObjectId(request.objectId());
        draft.setAuthorId(request.authorId());
        draft.setDescription(request.description());
        CityReport created = cityReportService.create(draft);
        return ResponseEntity.created(URI.create("/api/city-reports/" + created.getId()))
                .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public CityReportResponse update(@PathVariable Long id, @Valid @RequestBody CityReportPatchRequest request) {
        CityReport changes = new CityReport();
        changes.setDescription(request.description());
        changes.setStatus(request.status());
        return toResponse(cityReportService.update(id, changes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cityReportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CityReportResponse toResponse(CityReport r) {
        return new CityReportResponse(r.getId(), r.getObjectId(), r.getAuthorId(), r.getDescription(),
                r.getStatus(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
