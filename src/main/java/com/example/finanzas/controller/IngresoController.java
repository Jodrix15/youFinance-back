package com.example.finanzas.controller;

import com.example.finanzas.dto.ingreso.EvolucionIngresoResponse;
import com.example.finanzas.dto.ingreso.IngresoCategoriaResponse;
import com.example.finanzas.dto.ingreso.ResumenIngresosResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.IngresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    /** Total de ingresos y reparto por familia. Filtra opcionalmente por año/mes. */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenIngresosResponse> getResumen(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(ingresoService.getResumen(user, anio, mes));
    }

    /** Total de ingresos por categoría, de mayor a menor. */
    @GetMapping("/por-categoria")
    public ResponseEntity<List<IngresoCategoriaResponse>> getPorCategoria(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(ingresoService.getPorCategoria(user, anio, mes));
    }

    /** Serie mensual de ingresos totales (para la curva de evolución). */
    @GetMapping("/evolucion")
    public ResponseEntity<List<EvolucionIngresoResponse>> getEvolucion(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(ingresoService.getEvolucion(user));
    }
}
