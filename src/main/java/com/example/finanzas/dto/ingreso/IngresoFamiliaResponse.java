package com.example.finanzas.dto.ingreso;

import com.example.finanzas.model.enums.OrigenIngresoEnum;

import java.math.BigDecimal;

/**
 * Total de ingresos de una familia (Activo / Pasivo / Inversión) y su peso
 * sobre el total del periodo. {@code familia} puede ser null para los ingresos
 * cuya categoría aún no tiene familia asignada ("Sin clasificar").
 */
public record IngresoFamiliaResponse(
        OrigenIngresoEnum familia,
        BigDecimal total,
        BigDecimal porcentaje
) {
}
