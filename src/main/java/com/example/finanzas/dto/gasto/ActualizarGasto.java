package com.example.finanzas.dto.gasto;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Edición de un gasto recurrente. `fechaPrimerPago` corrige el alta del periodo
 * vigente y solo se aplica si no hay cambio de estado: activar o desactivar
 * abre o cierra periodo, y esas fechas las pone el servidor.
 * La fecha de último pago no se toca aquí, la lleva el registro de pagos.
 */
public record ActualizarGasto(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId,
        @NotNull(message = "El tipo de pago es obligatorio") TipoPagoEnum tipoPago,
        @NotNull(message = "La frecuencia es obligatoria") FrecuenciaEnum frecuencia,
        @NotNull(message = "La fecha de primer pago es obligatoria") LocalDate fechaPrimerPago,
        boolean active
) {
}
