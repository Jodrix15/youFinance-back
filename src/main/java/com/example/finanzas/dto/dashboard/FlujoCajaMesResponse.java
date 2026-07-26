package com.example.finanzas.dto.dashboard;

import java.math.BigDecimal;

/** Ingresos y gastos de un mes (1-12) de un año concreto. */
public record FlujoCajaMesResponse(
        int mes,
        BigDecimal ingresos,
        BigDecimal gastos
) {
}
