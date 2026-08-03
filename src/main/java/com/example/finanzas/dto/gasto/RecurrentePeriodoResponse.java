package com.example.finanzas.dto.gasto;

import com.example.finanzas.model.Gastos.RecurrentePeriodoEntity;

import java.time.LocalDate;

/** Un tramo de alta/baja del gasto recurrente. `fechaFin` null = sigue abierto. */
public record RecurrentePeriodoResponse(
        Long id,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaUltimoPago
) {
    public static RecurrentePeriodoResponse from(RecurrentePeriodoEntity periodo) {
        return new RecurrentePeriodoResponse(
                periodo.getId(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                periodo.getFechaUltimoPago()
        );
    }
}
