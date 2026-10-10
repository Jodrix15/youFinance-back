package com.example.finanzas.dto.inversion;

import java.math.BigDecimal;

/** Ingresos de una cuenta de inversión en un mes (serie de 12 meses, huecos a cero). */
public record RendimientoMesResponse(int anio, int mes, BigDecimal importe) {
}
