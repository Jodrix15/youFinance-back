package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.enums.PlataformaInversionEnum;
import com.example.finanzas.model.enums.TipoCuentaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Alta/edición de una cuenta.
 *
 * @param tipo              CORRIENTE o INVERSION. Opcional: al crear, null = CORRIENTE;
 *                          al editar, null = no cambia el tipo ni los campos de inversión
 *                          (compatibilidad con el formulario de cuentas corrientes).
 * @param plataforma        solo cuentas de inversión.
 * @param color             hex #rrggbb, opcional.
 * @param cuentaVinculadaId solo cuentas de inversión: cuenta corriente por defecto al retirar.
 */
public record CuentaDTO(
        @NotBlank(message = "El nombre de la cuenta es obligatorio") String nombreCuenta,
        @NotNull(message = "El saldo inicial es obligatorio")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo") BigDecimal saldoInicial,
        TipoCuentaEnum tipo,
        PlataformaInversionEnum plataforma,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color debe tener formato #rrggbb") String color,
        Long cuentaVinculadaId
) {
}
