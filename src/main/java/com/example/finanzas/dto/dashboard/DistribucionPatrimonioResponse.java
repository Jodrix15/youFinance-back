package com.example.finanzas.dto.dashboard;

import java.math.BigDecimal;

/**
 * Reparto del patrimonio para el widget de distribución: incluye las cuentas y
 * cada categoría de inversión (deudas excluidas). El porcentaje se calcula sobre
 * el total de cuentas + inversiones.
 */
public record DistribucionPatrimonioResponse(
        String concepto,
        BigDecimal importe,
        BigDecimal porcentaje
) {
}
