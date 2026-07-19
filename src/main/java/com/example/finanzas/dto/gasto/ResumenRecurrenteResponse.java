package com.example.finanzas.dto.gasto;

import java.math.BigDecimal;

/**
 * Totales agregados de gastos recurrentes / suscripciones (según tipoPago),
 * calculados en el backend. El coste anual se normaliza a mensual (÷12).
 */
public record ResumenRecurrenteResponse(
        BigDecimal gastoMensual,
        BigDecimal gastoAnual,
        long activos,
        long total
) {
}
