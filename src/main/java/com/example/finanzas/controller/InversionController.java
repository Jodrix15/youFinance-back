package com.example.finanzas.controller;

import com.example.finanzas.dto.inversion.ActualizarInversionDTO;
import com.example.finanzas.dto.inversion.AsignarCuentaDTO;
import com.example.finanzas.dto.inversion.CuentaInversionResponse;
import com.example.finanzas.dto.inversion.RendimientoMesResponse;
import com.example.finanzas.dto.inversion.RepartirInversionDTO;
import com.example.finanzas.dto.inversion.RetiradaDTO;
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

    @PutMapping("/{id}/cuenta")
    public ResponseEntity<InversionResponse> asignarCuenta(@PathVariable Long id,
                                                           @RequestBody AsignarCuentaDTO dto,
                                                           @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(InversionResponse.from(inversionService.asignarCuenta(id, dto.cuentaId(), user)));
    }

    /** Reparte la posición entre una o varias cuentas de inversión (sin mover efectivo). */
    @PostMapping("/{id}/reparto")
    public ResponseEntity<List<InversionResponse>> repartir(@PathVariable Long id,
                                                            @Valid @RequestBody RepartirInversionDTO dto,
                                                            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.repartir(id, dto, user).stream()
                .map(InversionResponse::from)
                .toList());
    }

    // ── Cuentas de inversión ────────────────────────────────────────────────

    @GetMapping("/cuentas")
    public ResponseEntity<List<CuentaInversionResponse>> getCuentas(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.getCuentasInversion(user));
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaInversionResponse> getCuenta(@PathVariable Long cuentaId,
                                                             @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.getCuentaInversion(cuentaId, user));
    }

    @GetMapping("/cuentas/{cuentaId}/rendimientos")
    public ResponseEntity<List<RendimientoMesResponse>> getRendimientos(@PathVariable Long cuentaId,
                                                                        @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.getRendimientosMensuales(cuentaId, user));
    }

    @PostMapping("/cuentas/{cuentaId}/retirada")
    public ResponseEntity<Void> retirar(@PathVariable Long cuentaId,
                                        @Valid @RequestBody RetiradaDTO dto,
                                        @AuthenticationPrincipal UserEntity user) {
        inversionService.retirar(cuentaId, dto, user);
        return ResponseEntity.noContent().build();
    }
}
