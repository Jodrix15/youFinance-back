package com.example.finanzas.controller;

import com.example.finanzas.dto.movimiento.MovimientoResponse;
import com.example.finanzas.dto.movimiento.MovimientosPageResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import com.example.finanzas.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final MovimientoService movimientoService;

    /**
     * Histórico global paginado, filtrable (tipo, cuenta, texto) y ordenable
     * (p. ej. ?sort=importe,desc). Incluye el resumen de totales del filtro.
     */
    @GetMapping
    public ResponseEntity<MovimientosPageResponse> buscar(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(required = false) TipoMovimientoEnum tipo,
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaTransaccion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(movimientoService.buscar(user, tipo, cuentaId, anio, mes, q, pageable));
    }

    /** Todos los movimientos del usuario en una sola consulta (para agregados). */
    @GetMapping("/todas")
    public ResponseEntity<List<MovimientoResponse>> todas(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(movimientoService.getTodas(user));
    }
}
