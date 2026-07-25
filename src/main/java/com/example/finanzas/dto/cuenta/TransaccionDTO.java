package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionDTO(
        @NotNull(message = "El tipo de movimiento es obligatorio") TipoMovimientoEnum tipoMovimiento,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId,
        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe debe ser mayor que cero") BigDecimal importe,
        String descripcion,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha
) {
}
