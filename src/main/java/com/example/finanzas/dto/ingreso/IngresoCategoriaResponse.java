package com.example.finanzas.dto.ingreso;

import com.example.finanzas.model.enums.OrigenIngresoEnum;

import java.math.BigDecimal;

/** Total de ingresos de una categoría, con la familia a la que pertenece. */
public record IngresoCategoriaResponse(
        String categoria,
        OrigenIngresoEnum familia,
        BigDecimal total
) {
}
