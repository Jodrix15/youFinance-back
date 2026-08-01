package com.example.finanzas.dto.dashboard;

import java.math.BigDecimal;

/**
 * Total gastado en una categoría (widget de gastos por categoría).
 * {@code color} es el hex elegido para la categoría; puede ser null en
 * categorías antiguas y el front cae entonces en la paleta por posición.
 */
public record GastoCategoriaResponse(
        String categoria,
        String color,
        BigDecimal total
) {
}
