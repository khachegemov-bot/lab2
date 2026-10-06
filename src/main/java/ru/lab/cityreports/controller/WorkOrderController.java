package ru.lab.cityreports.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lab.cityreports.dto.WorkOrderCreateRequest;
import ru.lab.cityreports.dto.WorkOrderPatchRequest;
import ru.lab.cityreports.dto.WorkOrderResponse;
import ru.lab.cityreports.model.WorkOrder;
import ru.lab.cityreports.service.WorkOrderService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public List<WorkOrderResponse> getAll() {
        return workOrderService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse getById(@PathVariable Long id) {
        return toResponse(workOrderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        WorkOrder draft = new WorkOrder();
        draft.setReportId(request.reportId());
        draft.setCrewId(request.crewId());
        draft.setDeadline(request.deadline());
        draft.setResultDescription(request.resultDescription());
        WorkOrder created = workOrderService.create(draft);
        return ResponseEntity.created(URI.create("/api/work-orders/" + created.getId()))
                .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public WorkOrderResponse update(@PathVariable Long id, @Valid @RequestBody WorkOrderPatchRequest request) {
        WorkOrder changes = new WorkOrder();
        changes.setCrewId(request.crewId());
        changes.setDeadline(request.deadline());
        changes.setStatus(request.status());
        changes.setResultDescription(request.resultDescription());
        return toResponse(workOrderService.update(id, changes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private WorkOrderResponse toResponse(WorkOrder o) {
        return new WorkOrderResponse(o.getId(), o.getReportId(), o.getCrewId(), o.getDeadline(),
                o.getStatus(), o.getResultDescription(), o.getCreatedAt(), o.getUpdatedAt(), o.getCompletedAt());
    }
}
