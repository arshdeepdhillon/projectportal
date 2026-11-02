package com.bu.service.persistenceprobe.controller;

/*
 * AI Contribution Weight: [100%] AI Generated / [0%] Human Refactored
 */

import com.bu.model.PersistenceRecord;
import com.bu.service.persistenceprobe.featureservice.PersistenceRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/records")
public class PersistenceRecordController {
    private final PersistenceRecordService service;

    public PersistenceRecordController(PersistenceRecordService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RecordResponse> save(@Valid @RequestBody RecordRequest request) {
        RecordResponse response = toResponse(service.save(request.value()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public RecordResponse getById(@PathVariable UUID id) {
        return toResponse(service.getById(id));
    }

    private RecordResponse toResponse(PersistenceRecord record) {
        return new RecordResponse(record.getId(), record.getValue());
    }

    public record RecordRequest(@NotBlank @Size(max = 255) String value) {
    }

    public record RecordResponse(UUID id, String value) {
    }
}
