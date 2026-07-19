package com.example.finanzas.dto.movimiento;

import com.example.finanzas.model.TransaccionEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Transacción "global" (incluye el nombre de la cuenta) para el histórico. */
public record MovimientoResponse(
        Long id,
        Long cuentaId,
        String cuentaNombre,
        TipoMovimientoEnum tipoMovimiento,
        Long categoriaId,
        String categoriaNombre,
        BigDecimal importe,
        String descripcion,
        LocalDate fechaTransaccion
) {
    public static MovimientoResponse from(TransaccionEntity t) {
        return new MovimientoResponse(
                t.getId(),
                t.getCuenta() != null ? t.getCuenta().getId() : null,
                t.getCuenta() != null ? t.getCuenta().getNombreCuenta() : null,
                t.getTipoMovimiento(),
                t.getCategoria() != null ? t.getCategoria().getId() : null,
                t.getCategoria() != null ? t.getCategoria().getNombreCategoria() : null,
                t.getImporte(),
                t.getDescripcion(),
                t.getFechaTransaccion()
        );
    }
}
