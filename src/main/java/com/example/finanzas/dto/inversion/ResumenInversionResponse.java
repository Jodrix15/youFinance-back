package com.example.finanzas.dto.inversion;

import java.math.BigDecimal;

/**
 * Totales agregados de las inversiones del usuario, calculados en el backend
 * para que el front no tenga que recalcularlos.
 */
public record ResumenInversionResponse(
        BigDecimal importeTotal,
        BigDecimal capitalAportadoTotal,
        BigDecimal plusvaliaTotal,
        BigDecimal porcentajeTotal
) {
}
