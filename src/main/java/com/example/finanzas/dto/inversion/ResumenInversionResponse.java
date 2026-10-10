package com.example.finanzas.dto.inversion;

import java.math.BigDecimal;

/**
 * Totales agregados de las inversiones del usuario, calculados en el backend
 * para que el front no tenga que recalcularlos.
 *
 * @param importeTotal     valor actual de todas las posiciones.
 * @param efectivoTotal    efectivo sin invertir en las cuentas de inversión.
 * @param valorTotal       importeTotal + efectivoTotal.
 * @param rendimientos12m  ingresos cobrados en cuentas de inversión en los últimos 12 meses
 *                         (mes actual incluido).
 */
public record ResumenInversionResponse(
        BigDecimal importeTotal,
        BigDecimal capitalAportadoTotal,
        BigDecimal plusvaliaTotal,
        BigDecimal porcentajeTotal,
        BigDecimal efectivoTotal,
        BigDecimal valorTotal,
        BigDecimal rendimientos12m
) {
}
