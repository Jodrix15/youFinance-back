package com.example.finanzas.dto.inversion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Alta de una posición.
 *
 * @param nombre        activo concreto (p. ej. "MSCI World"). Opcional.
 * @param cuentaId      cuenta de inversión donde está la posición. Opcional.
 * @param desdeEfectivo si es true (y hay cuenta), el capital aportado se descuenta
 *                      del efectivo de la cuenta con un movimiento de tipo INVERSION.
 *                      False para dar de alta posiciones que ya tenías.
 */
public record InversionDTO(
        @NotNull Long categoriaId,
        @Size(max = 120) String nombre,
        @NotNull @Positive BigDecimal capitalAportado,
        @NotNull @PositiveOrZero BigDecimal capitalTotal,
        Long cuentaId,
        Boolean desdeEfectivo
) {}
