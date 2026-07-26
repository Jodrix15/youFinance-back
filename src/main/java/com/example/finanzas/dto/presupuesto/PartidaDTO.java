package com.example.finanzas.dto.presupuesto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Partida de un presupuesto. Debe tener una categoría (categoriaId) o un nombre
 * de línea libre. El importe es lo presupuestado para esa partida.
 */
public record PartidaDTO(
        Long categoriaId,
        String nombre,
        @NotNull(message = "El importe de la partida es obligatorio")
        @PositiveOrZero(message = "El importe de la partida no puede ser negativo") BigDecimal importe
) {
    @AssertTrue(message = "Cada partida debe tener una categoría o un nombre")
    public boolean isPartidaValida() {
        return categoriaId != null || (nombre != null && !nombre.isBlank());
    }
}
