package com.example.finanzas.dto.gasto;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alta de un gasto recurrente. Siempre nace activo, con su primer periodo
 * abierto: la baja se hace después con {@link ActualizarGasto}.
 */
public record CrearGasto(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId,
        @NotNull(message = "El tipo de pago es obligatorio") TipoPagoEnum tipoPago,
        @NotNull(message = "La frecuencia es obligatoria") FrecuenciaEnum frecuencia,
        @NotNull(message = "La fecha de primer pago es obligatoria") LocalDate fechaPrimerPago,
        @NotNull(message = "El importe inicial es obligatorio")
        @Positive(message = "El importe inicial debe ser mayor que cero") BigDecimal importeInicial
) {
}
