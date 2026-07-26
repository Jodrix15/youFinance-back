package com.example.finanzas.dto.dashboard;

import java.math.BigDecimal;

/** Total gastado en una categoría (widget de gastos por categoría). */
public record GastoCategoriaResponse(
        String categoria,
        BigDecimal total
) {
}
