package com.example.finanzas.dto.cuenta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CuentaDTO(
        @NotBlank(message = "El nombre de la cuenta es obligatorio") String nombreCuenta,
        @NotNull(message = "El saldo inicial es obligatorio")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo") BigDecimal importe
) {
}
