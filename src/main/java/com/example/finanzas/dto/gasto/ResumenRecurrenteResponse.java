package com.example.finanzas.dto.gasto;

import java.math.BigDecimal;

/**
 * Totales agregados de gastos recurrentes / suscripciones (según tipoPago),
 * calculados en el backend. El coste anual se normaliza a mensual (÷12).
 * gastoMensualFijo + gastoMensualVariable = gastoMensual (desglose por tipo de importe).
 */
public record ResumenRecurrenteResponse(
        BigDecimal gastoMensual,
        BigDecimal gastoMensualFijo,
        BigDecimal gastoMensualVariable,
        BigDecimal gastoAnual,
        long activos,
        long total
) {
}
