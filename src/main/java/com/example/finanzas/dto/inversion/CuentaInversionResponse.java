package com.example.finanzas.dto.inversion;

import com.example.finanzas.model.enums.PlataformaInversionEnum;

import java.math.BigDecimal;

/**
 * Una cuenta de inversión con los totales de sus posiciones (tarjetas y detalle).
 *
 * @param efectivo          saldo de la cuenta: dinero sin invertir.
 * @param capitalAportado   suma del aportado de sus posiciones.
 * @param valorPosiciones   suma del valor actual de sus posiciones.
 * @param valorTotal        efectivo + valorPosiciones.
 * @param plusvalia         valorPosiciones - capitalAportado.
 * @param rendimientos12m   ingresos registrados en la cuenta en los últimos 12 meses.
 */
public record CuentaInversionResponse(
        Long id,
        String nombre,
        PlataformaInversionEnum plataforma,
        String color,
        Long cuentaVinculadaId,
        String cuentaVinculadaNombre,
        BigDecimal saldoInicial,
        BigDecimal efectivo,
        BigDecimal capitalAportado,
        BigDecimal valorPosiciones,
        BigDecimal valorTotal,
        BigDecimal plusvalia,
        BigDecimal porcentajePlusvalia,
        BigDecimal rendimientos12m,
        int numPosiciones
) {
}
