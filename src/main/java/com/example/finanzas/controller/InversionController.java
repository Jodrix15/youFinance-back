package com.example.finanzas.controller;

import com.example.finanzas.dto.inversion.ActualizarInversionDTO;
import com.example.finanzas.dto.inversion.DistribucionCategoriaResponse;
import com.example.finanzas.dto.inversion.InversionDTO;
import com.example.finanzas.dto.inversion.InversionResponse;
import com.example.finanzas.dto.inversion.ResumenInversionResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.InversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inversion")
@RequiredArgsConstructor
public class InversionController {

    private final InversionService inversionService;

    @GetMapping
    public ResponseEntity<List<InversionResponse>> getAllInversiones(@AuthenticationPrincipal UserEntity user) {
        List<InversionResponse> inversiones = inversionService.getAllInversiones(user).stream()
                .map(InversionResponse::from)
                .toList();
        return ResponseEntity.ok(inversiones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InversionResponse> getInversion(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(InversionResponse.from(inversionService.getInversionById(id, user)));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenInversionResponse> getResumen(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.getResumen(user));
    }

    @GetMapping("/distribucion-categoria")
    public ResponseEntity<List<DistribucionCategoriaResponse>> getDistribucionPorCategoria(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.getDistribucionPorCategoria(user));
    }

    @PostMapping
    public ResponseEntity<InversionResponse> add(@Valid @RequestBody InversionDTO inversionDTO,
                                                 @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InversionResponse.from(inversionService.add(inversionDTO, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InversionResponse> actualizar(@Valid @RequestBody ActualizarInversionDTO actualizarInversionDTO,
                                                        @PathVariable Long id,
                                                        @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(InversionResponse.from(inversionService.actualizar(id, actualizarInversionDTO, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id,
                                       @AuthenticationPrincipal UserEntity user) {
        inversionService.remove(id, user);
        return ResponseEntity.noContent().build();
    }
}