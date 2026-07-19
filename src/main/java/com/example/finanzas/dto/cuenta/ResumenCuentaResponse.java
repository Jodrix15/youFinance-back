package com.example.finanzas.dto.cuenta;

import java.math.BigDecimal;

/**
 * Totales de cuentas y agregados de movimientos del periodo (año/mes opcionales),
 * calculados en el backend.
 */
public record ResumenCuentaResponse(
        BigDecimal totalCuentas,
        BigDecimal ingresos,
        BigDecimal gastos,
        BigDecimal diferencia,
        long numeroCuentas
) {
}
