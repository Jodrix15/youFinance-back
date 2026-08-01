package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.CuentaEntity;

import java.math.BigDecimal;

/**
 * @param saldoInicial saldo de partida que introdujo el usuario al crear la cuenta.
 * @param saldo        saldo actual: saldoInicial + suma de movimientos. Calculado, nunca almacenado.
 */
public record CuentaResponse(
        Long id,
        String nombreCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldo
) {
    public static CuentaResponse from(CuentaEntity cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNombreCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldo()
        );
    }
}
