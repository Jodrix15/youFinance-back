package com.example.finanzas.dto.cuenta;

import com.example.finanzas.model.CuentaEntity;

import java.math.BigDecimal;

public record CuentaResponse(
        Long id,
        String nombreCuenta,
        BigDecimal importe
) {
    public static CuentaResponse from(CuentaEntity cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNombreCuenta(),
                cuenta.getImporte()
        );
    }
}