package com.example.finanzas.dto.ingreso;

import java.math.BigDecimal;

/**
 * Total de ingresos de un mes para la curva de evolución. {@code mes} es el
 * primer día del mes en formato ISO ('YYYY-MM-DD'), coherente con el histórico
 * de patrimonio, para poder filtrar por rango en el cliente.
 */
public record EvolucionIngresoResponse(
        String mes,
        BigDecimal total
) {
}
