package ru.lab.cityreports.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lab.cityreports.dto.CityObjectCreateRequest;
import ru.lab.cityreports.dto.CityObjectPatchRequest;
import ru.lab.cityreports.dto.CityObjectResponse;
import ru.lab.cityreports.model.CityObject;
import ru.lab.cityreports.service.CityObjectService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/city-objects")
public class CityObjectController {

    private final CityObjectService cityObjectService;

    public CityObjectController(CityObjectService cityObjectService) {
        this.cityObjectService = cityObjectService;
    }

    @GetMapping
    public List<CityObjectResponse> getAll() {
        return cityObjectService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CityObjectResponse getById(@PathVariable Long id) {
        return toResponse(cityObjectService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CityObjectResponse> create(@Valid @RequestBody CityObjectCreateRequest request) {
        CityObject draft = new CityObject();
        draft.setAddress(request.address());
        draft.setType(request.type());
        draft.setState(request.state());
        CityObject created = cityObjectService.create(draft);
        return ResponseEntity.created(URI.create("/api/city-objects/" + created.getId()))
                .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public CityObjectResponse update(@PathVariable Long id, @Valid @RequestBody CityObjectPatchRequest request) {
        CityObject changes = new CityObject();
        changes.setAddress(request.address());
        changes.setType(request.type());
        changes.setState(request.state());
        return toResponse(cityObjectService.update(id, changes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cityObjectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CityObjectResponse toResponse(CityObject o) {
        return new CityObjectResponse(o.getId(), o.getAddress(), o.getType(), o.getState(),
                o.getCreatedAt(), o.getUpdatedAt());
    }
}
