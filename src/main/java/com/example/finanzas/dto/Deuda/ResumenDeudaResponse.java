package com.example.finanzas.dto.Deuda;

import java.math.BigDecimal;

/**
 * Totales agregados de las deudas del usuario, calculados en el backend.
 */
public record ResumenDeudaResponse(
        BigDecimal totalPendiente,
        BigDecimal totalPagado,
        BigDecimal totalConIntereses,
        BigDecimal gastoMensualEstimado,
        long numeroDeudas
) {
}
