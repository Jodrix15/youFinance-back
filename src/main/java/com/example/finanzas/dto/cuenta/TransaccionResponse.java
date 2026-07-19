package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.TransaccionEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionResponse(
        Long id,
        Long cuentaId,
        TipoMovimientoEnum tipoMovimiento,
        Long categoriaId,
        String categoriaNombre,
        BigDecimal importe,
        String descripcion,
        LocalDate fechaTransaccion
) {
    public static TransaccionResponse from(TransaccionEntity transaccion) {
        return new TransaccionResponse(
                transaccion.getId(),
                transaccion.getCuenta() != null ? transaccion.getCuenta().getId() : null,
                transaccion.getTipoMovimiento(),
                transaccion.getCategoria() != null ? transaccion.getCategoria().getId() : null,
                transaccion.getCategoria() != null ? transaccion.getCategoria().getNombreCategoria() : null,
                transaccion.getImporte(),
                transaccion.getDescripcion(),
                transaccion.getFechaTransaccion()
        );
    }
}