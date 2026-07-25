package com.example.finanzas.dto.dashboard;

import java.math.BigDecimal;

/** Desglose del gasto fijo que vence en un mes concreto. */
public record GastosFijosMesResponse(
        BigDecimal suscripciones,
        BigDecimal recurrentes,
        BigDecimal cuotasDeuda,
        BigDecimal total
) {
}
