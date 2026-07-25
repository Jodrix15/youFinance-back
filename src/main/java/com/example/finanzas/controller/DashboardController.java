package com.example.finanzas.controller;

import com.example.finanzas.dto.dashboard.DistribucionPatrimonioResponse;
import com.example.finanzas.dto.dashboard.FlujoCajaMesResponse;
import com.example.finanzas.dto.dashboard.GastoCategoriaResponse;
import com.example.finanzas.dto.dashboard.GastosFijosMesResponse;
import com.example.finanzas.dto.dashboard.PatrimonioSnapshotResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.CuentaService;
import com.example.finanzas.service.DashboardService;
import com.example.finanzas.service.DeudaService;
import com.example.finanzas.service.InversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CuentaService cuentaService;
    private final InversionService inversionService;
    private final DeudaService deudaService;

    @GetMapping("/patrimonio-neto")
    public ResponseEntity<BigDecimal> getPatrimonioNeto(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(dashboardService.getPatrimonioNeto(user));
    }

    /**
     * Histórico mensual de patrimonio. Antes de devolverlo, actualiza (upsert)
     * la foto del mes en curso con los valores actuales, de modo que el último
     * punto de la curva siempre refleje el patrimonio de hoy.
     */
    @GetMapping("/patrimonio/historico")
    public ResponseEntity<List<PatrimonioSnapshotResponse>> getHistorico(@AuthenticationPrincipal UserEntity user) {
        dashboardService.capturarSnapshot(user);
        List<PatrimonioSnapshotResponse> historico = dashboardService.getHistorico(user).stream()
                .map(PatrimonioSnapshotResponse::from)
                .toList();
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/capital-cuentas")
    public ResponseEntity<BigDecimal> getSalarioCuentas(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(cuentaService.getImporteTotal(user));
    }

    @GetMapping("/capital-inversion")
    public ResponseEntity<BigDecimal> getCapitalInversiones(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(inversionService.getImporteTotal(user));
    }

    @GetMapping("/capital-deuda")
    public ResponseEntity<BigDecimal> getCapitalDeudas(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(deudaService.getImporteTotal(user));
    }

    @GetMapping("/distribucion-patrimonio")
    public ResponseEntity<List<DistribucionPatrimonioResponse>> getDistribucionPatrimonio(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(dashboardService.getDistribucionPatrimonio(user));
    }

    /** Ingresos y gastos por mes del año indicado (por defecto, el actual). */
    @GetMapping("/flujo-caja")
    public ResponseEntity<List<FlujoCajaMesResponse>> getFlujoCaja(
            @RequestParam(required = false) Integer anio,
            @AuthenticationPrincipal UserEntity user) {
        int year = anio != null ? anio : java.time.LocalDate.now().getYear();
        return ResponseEntity.ok(dashboardService.getFlujoCaja(user, year));
    }

    /** Total gastado por categoría, de mayor a menor. */
    @GetMapping("/gastos-categoria")
    public ResponseEntity<List<GastoCategoriaResponse>> getGastosPorCategoria(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(dashboardService.getGastosPorCategoria(user));
    }

    /** Gasto fijo que vence en el mes indicado (por defecto, el actual). */
    @GetMapping("/gastos-fijos")
    public ResponseEntity<GastosFijosMesResponse> getGastosFijosMes(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @AuthenticationPrincipal UserEntity user) {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        int year = anio != null ? anio : hoy.getYear();
        int month = mes != null ? mes : hoy.getMonthValue();
        return ResponseEntity.ok(dashboardService.getGastosFijosMes(user, year, month));
    }



}
