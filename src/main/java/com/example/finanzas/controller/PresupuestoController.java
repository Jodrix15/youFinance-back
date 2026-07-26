package com.example.finanzas.controller;

import com.example.finanzas.dto.presupuesto.PresupuestoDTO;
import com.example.finanzas.dto.presupuesto.PresupuestoResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.PresupuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presupuesto")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService service;

    @GetMapping
    public ResponseEntity<List<PresupuestoResponse>> getAll(@AuthenticationPrincipal UserEntity user) {
        List<PresupuestoResponse> presupuestos = service.getAll(user).stream()
                .map(PresupuestoResponse::from)
                .toList();
        return ResponseEntity.ok(presupuestos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoResponse> get(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(PresupuestoResponse.from(service.get(id, user)));
    }

    @PostMapping
    public ResponseEntity<PresupuestoResponse> crear(@Valid @RequestBody PresupuestoDTO dto,
                                                     @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PresupuestoResponse.from(service.crear(dto, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody PresupuestoDTO dto,
                                                      @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(PresupuestoResponse.from(service.update(id, dto, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id,
                                       @AuthenticationPrincipal UserEntity user) {
        service.remove(id, user);
        return ResponseEntity.noContent().build();
    }
}
