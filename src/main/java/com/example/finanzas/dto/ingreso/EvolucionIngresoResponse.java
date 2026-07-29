package com.example.finanzas.dto.ingreso;

import java.math.BigDecimal;

/**
 * Ingresos de un mes para la curva de evolución: el total y su desglose por
 * familia. {@code mes} es el primer día del mes en formato ISO ('YYYY-MM-DD'),
 * coherente con el histórico de patrimonio, para poder filtrar por rango en el
 * cliente. {@code sinClasificar} recoge los ingresos cuya categoría aún no
 * tiene familia, de modo que las partes siempre suman el total.
 */
public record EvolucionIngresoResponse(
        String mes,
        BigDecimal total,
        BigDecimal activo,
        BigDecimal pasivo,
        BigDecimal inversion,
        BigDecimal sinClasificar
) {
}
