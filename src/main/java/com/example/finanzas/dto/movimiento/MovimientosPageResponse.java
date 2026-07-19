package com.example.finanzas.dto.movimiento;

import java.math.BigDecimal;
import java.util.List;

/**
 * Una página de movimientos más el resumen (totales) del conjunto filtrado
 * completo, no solo de la página, para que los KPIs sean correctos.
 */
public record MovimientosPageResponse(
        List<MovimientoResponse> contenido,
        int pagina,
        int size,
        long totalElementos,
        int totalPaginas,
        BigDecimal ingresos,
        BigDecimal gastos,
        BigDecimal inversiones,
        BigDecimal diferencia
) {
}
