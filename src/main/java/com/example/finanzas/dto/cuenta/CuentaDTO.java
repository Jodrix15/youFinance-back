package com.example.finanzas.dto.cuenta;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CuentaDTO(
        @NotNull String nombreCuenta,
        @NotNull BigDecimal importe
) {
}
