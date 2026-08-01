package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param categoriaId    obligatorio salvo en TRANSFERENCIA, donde debe venir vacío.
 * @param cuentaDestinoId obligatorio solo en TRANSFERENCIA; debe ser distinto de la cuenta origen.
 *                        La coherencia entre tipo, categoría y destino se valida en el servicio,
 *                        que es donde se puede comprobar la propiedad de la cuenta destino.
 */
public record TransaccionDTO(
        @NotNull(message = "El tipo de movimiento es obligatorio") TipoMovimientoEnum tipoMovimiento,
        Long categoriaId,
        Long cuentaDestinoId,
        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe debe ser mayor que cero") BigDecimal importe,
        String descripcion,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha
) {
}
