package com.obs.controller;

import com.obs.dto.BolumDTO;
import com.obs.service.BolumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bolumler")
@RequiredArgsConstructor
public class BolumController {

    private final BolumService bolumService;

    @GetMapping
    public ResponseEntity<List<BolumDTO.Response>> getAll() {
        return ResponseEntity.ok(bolumService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BolumDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bolumService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BolumDTO.Response> create(@RequestBody BolumDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bolumService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BolumDTO.Response> update(@PathVariable Long id, @RequestBody BolumDTO.Request request) {
        return ResponseEntity.ok(bolumService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bolumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
