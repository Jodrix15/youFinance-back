package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionDTO(
        @NotNull TipoMovimientoEnum tipoMovimiento,
        @NotNull Long categoriaId,
        @NotNull BigDecimal importe,
        String descripcion,
        @NotNull LocalDate fecha
) {
}
