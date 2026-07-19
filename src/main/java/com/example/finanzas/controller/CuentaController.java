package com.example.finanzas.controller;

import com.example.finanzas.dto.cuenta.CuentaDTO;
import com.example.finanzas.dto.cuenta.CuentaResponse;
import com.example.finanzas.dto.cuenta.ResumenCuentaResponse;
import com.example.finanzas.dto.cuenta.TransaccionDTO;
import com.example.finanzas.dto.cuenta.TransaccionResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuenta")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService service;

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> getAllCuentas(@AuthenticationPrincipal UserEntity user) {
        List<CuentaResponse> cuentas = service.getAllCuentas(user).stream()
                .map(CuentaResponse::from)
                .toList();
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenCuentaResponse> getResumen(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(service.getResumen(user, anio, mes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> getCuenta(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(CuentaResponse.from(service.getCuenta(id, user)));
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> addCuenta(@Valid @RequestBody CuentaDTO cuentaDTO,
                                                     @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CuentaResponse.from(service.addCuenta(user, cuentaDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> updateCuenta(@PathVariable Long id,
                                                        @Valid @RequestBody CuentaDTO cuentaDTO,
                                                        @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(CuentaResponse.from(service.updateCuenta(id, cuentaDTO, user)));
    }

    @GetMapping("/{id}/transacciones")
    public ResponseEntity<List<TransaccionResponse>> getAllTransacciones(@PathVariable Long id,
                                                                          @AuthenticationPrincipal UserEntity user) {
        List<TransaccionResponse> transacciones = service.getAllTransacciones(id, user).stream()
                .map(TransaccionResponse::from)
                .toList();
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/{id}/transacciones/{transaccionId}")
    public ResponseEntity<TransaccionResponse> getTransaccion(@PathVariable Long id,
                                                                @PathVariable Long transaccionId,
                                                                @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(TransaccionResponse.from(service.getTransaccion(id, transaccionId, user)));
    }

    @PostMapping("/{id}/transacciones")
    public ResponseEntity<TransaccionResponse> addTransaccionToCuenta(@PathVariable Long id,
                                                                       @Valid @RequestBody TransaccionDTO transaccionDTO,
                                                                       @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransaccionResponse.from(service.addTransaccionToCuenta(id, transaccionDTO, user)));
    }

    @PutMapping("/{id}/transacciones/{transaccionId}")
    public ResponseEntity<TransaccionResponse> updateTransaccion(@PathVariable Long id,
                                                                  @PathVariable Long transaccionId,
                                                                  @Valid @RequestBody TransaccionDTO transaccionDTO,
                                                                  @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(TransaccionResponse.from(service.updateTransaccion(id, transaccionId, transaccionDTO, user)));
    }

    @DeleteMapping("/{id}/transacciones/{transaccionId}")
    public ResponseEntity<Void> deleteTransaccion(@PathVariable Long id,
                                                  @PathVariable Long transaccionId,
                                                  @AuthenticationPrincipal UserEntity user) {
        service.deleteTransaccion(id, transaccionId, user);
        return ResponseEntity.noContent().build();
    }
}