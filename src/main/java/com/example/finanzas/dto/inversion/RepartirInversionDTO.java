package com.example.finanzas.dto.inversion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reparte una posición entre una o varias cuentas de inversión.
 *
 * <p>Cada tramo indica cuánto <b>valor actual</b> pasa a cada cuenta; el capital
 * aportado se reparte en la misma proporción. Lo que no se reparta se queda en
 * la posición original (en su cuenta actual). Cada tramo puede llevar su propio
 * activo (p. ej. "Acciones" → "Apple" y "Microsoft"), y una misma cuenta puede
 * repetirse con activos distintos. Solo cambia la asignación: no genera
 * movimientos de efectivo.
 */
public record RepartirInversionDTO(
        @NotEmpty(message = "Indica al menos una cuenta") @Valid List<Tramo> tramos
) {
    public record Tramo(
            @NotNull(message = "Indica la cuenta") Long cuentaId,
            /** Activo del tramo; vacío = conserva el de la posición original. */
            @Size(max = 120, message = "El nombre del activo admite 120 caracteres como máximo") String nombre,
            @NotNull(message = "Indica el importe")
            @Positive(message = "El importe debe ser mayor que cero") BigDecimal importe
    ) {}
}
