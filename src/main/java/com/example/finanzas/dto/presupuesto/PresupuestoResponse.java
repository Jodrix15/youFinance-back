package com.example.finanzas.dto.presupuesto;

import com.example.finanzas.model.PresupuestoEntity;
import com.example.finanzas.model.enums.PeriodoPresupuestoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PresupuestoResponse(
        Long id,
        String nombre,
        PeriodoPresupuestoEnum periodo,
        int anio,
        int mes,
        Integer semana,
        BigDecimal cantidadBase,
        boolean descontarGastosFijos,
        BigDecimal totalPresupuestado,
        LocalDate fechaCreacion,
        List<PartidaResponse> partidas
) {
    public static PresupuestoResponse from(PresupuestoEntity p) {
        List<PartidaResponse> partidas = p.getPartidas().stream()
                .map(PartidaResponse::from)
                .toList();

        BigDecimal total = p.getPartidas().stream()
                .map(pa -> pa.getImporte() != null ? pa.getImporte() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PresupuestoResponse(
                p.getId(),
                p.getNombre(),
                p.getPeriodo(),
                p.getAnio(),
                p.getMes(),
                p.getSemana(),
                p.getCantidadBase(),
                p.isDescontarGastosFijos(),
                total,
                p.getFechaCreacion(),
                partidas
        );
    }
}
