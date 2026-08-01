package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.TransaccionEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param transferenciaId          no nulo solo en transferencias; identifica el par de apuntes.
 * @param cuentaContrapartidaId    la otra cuenta del traspaso (destino si el importe es negativo,
 *                                 origen si es positivo).
 */
public record TransaccionResponse(
        Long id,
        Long cuentaId,
        TipoMovimientoEnum tipoMovimiento,
        Long categoriaId,
        String categoriaNombre,
        BigDecimal importe,
        String descripcion,
        LocalDate fechaTransaccion,
        String transferenciaId,
        Long cuentaContrapartidaId,
        String cuentaContrapartidaNombre
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
                transaccion.getFechaTransaccion(),
                transaccion.getTransferenciaId(),
                transaccion.getCuentaContrapartida() != null ? transaccion.getCuentaContrapartida().getId() : null,
                transaccion.getCuentaContrapartida() != null ? transaccion.getCuentaContrapartida().getNombreCuenta() : null
        );
    }
}
