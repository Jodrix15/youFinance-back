package com.example.finanzas.dto.ingreso;

import com.example.finanzas.model.enums.OrigenIngresoEnum;

import java.math.BigDecimal;

/**
 * Total de ingresos de una categoría, con la familia a la que pertenece y el
 * color con el que se pinta. {@code color} puede ser null en categorías
 * antiguas: el front cae entonces en la paleta por posición.
 */
public record IngresoCategoriaResponse(
        String categoria,
        OrigenIngresoEnum familia,
        String color,
        BigDecimal total
) {
}
