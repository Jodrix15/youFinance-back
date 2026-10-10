package com.example.finanzas.dto.inversion;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Actualización de una inversión. Ambos campos son opcionales, pero debe
 * indicarse al menos uno:
 *  - aportacion: se SUMA al capital aportado y al capital total.
 *  - valorActual: FIJA el capital total (valor/ganancia actual del momento).
 * Si se envían los dos, primero se aplica la aportación y después se fija el valor actual.
 *
 * @param nombre        nuevo nombre del activo; null = no cambia, vacío = se quita.
 * @param desdeEfectivo si es true y la posición está en una cuenta, la aportación
 *                      se descuenta del efectivo de esa cuenta.
 */
public record ActualizarInversionDTO(
        @PositiveOrZero BigDecimal aportacion,
        @PositiveOrZero BigDecimal valorActual,
        Boolean desdeEfectivo,
        @Size(max = 120) String nombre
) {}
