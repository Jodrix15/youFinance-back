package com.example.finanzas.controller;

import com.example.finanzas.dto.gasto.ActualizarGasto;
import com.example.finanzas.dto.gasto.CrearGasto;
import com.example.finanzas.dto.gasto.GastoRecurrenteResponse;
import com.example.finanzas.dto.gasto.NuevoPrecioRequest;
import com.example.finanzas.dto.gasto.RecurrentePrecioResponse;
import com.example.finanzas.dto.gasto.ResumenRecurrenteResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.service.GastoRecurrenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurrente")
@RequiredArgsConstructor
public class GastoRecurrenteController {

    private final GastoRecurrenteService service;

    @GetMapping
    public ResponseEntity<List<GastoRecurrenteResponse>> getAllGastosRecurrentes(@AuthenticationPrincipal UserEntity user) {
        List<GastoRecurrenteResponse> gastos = service.getAllGastosRecurrentes(user).stream()
                .map(GastoRecurrenteResponse::from)
                .toList();
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenRecurrenteResponse> getResumen(@RequestParam TipoPagoEnum tipo,
                                                                @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(service.getResumen(user, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoRecurrenteResponse> getGastoRecurrente(@PathVariable Long id,
                                                                      @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(GastoRecurrenteResponse.from(service.getGastoRecurrente(id, user)));
    }

    @PostMapping
    public ResponseEntity<GastoRecurrenteResponse> add(@Valid @RequestBody CrearGasto gastoRecurrente,
                                                       @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GastoRecurrenteResponse.from(service.add(gastoRecurrente, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id,
                                       @AuthenticationPrincipal UserEntity user) {
        service.remove(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoRecurrenteResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody ActualizarGasto gastoRecurrente,
                                                          @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(GastoRecurrenteResponse.from(service.update(id, gastoRecurrente, user)));
    }

    @PostMapping("/{id}/pago")
    public ResponseEntity<GastoRecurrenteResponse> registrarPago(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(GastoRecurrenteResponse.from(service.registrarPago(id, user)));
    }

    @PatchMapping("/{id}/precio")
    public ResponseEntity<RecurrentePrecioResponse> registrarNuevoPrecio(@PathVariable Long id,
                                                                         @Valid @RequestBody NuevoPrecioRequest request,
                                                                         @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(
                RecurrentePrecioResponse.from(service.registrarNuevoPrecio(id, request, user)));
    }
}

   