package com.example.finanzas.controller;

import com.example.finanzas.dto.Deuda.DeudaDTO;
import com.example.finanzas.dto.Deuda.DeudaDTOResponse;
import com.example.finanzas.dto.Deuda.ResumenDeudaResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.DeudaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/deuda")
@RequiredArgsConstructor
public class DeudaController {

    private final DeudaService service;

    @GetMapping
    public ResponseEntity<List<DeudaDTOResponse>> getAllDeudas(@AuthenticationPrincipal UserEntity user) {
        List<DeudaDTOResponse> deudas = service.getAllDeudas(user).stream()
                .map(DeudaDTOResponse::from)
                .toList();
        return ResponseEntity.ok(deudas);
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenDeudaResponse> getResumen(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(service.getResumen(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeudaDTOResponse> getDeuda(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(DeudaDTOResponse.from(service.getDeuda(id, user)));
    }

    @PostMapping
    public ResponseEntity<DeudaDTOResponse> crear(@Valid @RequestBody DeudaDTO deudaDTO,
                                                @AuthenticationPrincipal UserEntity user){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DeudaDTOResponse.from(service.crear(deudaDTO, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeudaDTOResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody DeudaDTO deudaDTO,
                                                   @AuthenticationPrincipal UserEntity user){
        return ResponseEntity.ok(DeudaDTOResponse.from(service.update(id, deudaDTO, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id,
                                       @AuthenticationPrincipal UserEntity user) {
        service.remove(id, user);
        return ResponseEntity.noContent().build();
    }
}
