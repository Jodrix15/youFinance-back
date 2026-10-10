package com.example.finanzas.dto.inversion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Retirada de dinero de una cuenta de inversión a otra cuenta (normalmente corriente).
 *
 * @param inversionId     posición de la que sale el dinero. Null = sale del efectivo de la cuenta.
 *                        Si se indica, primero se vende esa parte de la posición (pasa a efectivo)
 *                        y después se transfiere.
 * @param cuentaDestinoId cuenta que recibe el dinero.
 */
public record RetiradaDTO(
        Long inversionId,
        @NotNull(message = "Indica la cuenta de destino") Long cuentaDestinoId,
        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe debe ser mayor que cero") BigDecimal importe,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        String descripcion
) {}
