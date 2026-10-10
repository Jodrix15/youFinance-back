package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.CuentaEntity;
import com.example.finanzas.model.enums.PlataformaInversionEnum;
import com.example.finanzas.model.enums.TipoCuentaEnum;

import java.math.BigDecimal;

/**
 * @param saldoInicial saldo de partida que introdujo el usuario al crear la cuenta.
 * @param saldo        saldo actual: saldoInicial + suma de movimientos. Calculado, nunca almacenado.
 *                     En una cuenta de inversión es el efectivo sin invertir.
 */
public record CuentaResponse(
        Long id,
        String nombreCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldo,
        TipoCuentaEnum tipo,
        PlataformaInversionEnum plataforma,
        String color,
        Long cuentaVinculadaId,
        String cuentaVinculadaNombre
) {
    public static CuentaResponse from(CuentaEntity cuenta) {
        CuentaEntity vinculada = cuenta.getCuentaVinculada();
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNombreCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldo(),
                cuenta.getTipo(),
                cuenta.getPlataforma(),
                cuenta.getColor(),
                vinculada != null ? vinculada.getId() : null,
                vinculada != null ? vinculada.getNombreCuenta() : null
        );
    }
}
