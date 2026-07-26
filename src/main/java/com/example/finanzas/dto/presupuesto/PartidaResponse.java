package com.example.finanzas.dto.presupuesto;

import com.example.finanzas.model.PresupuestoPartidaEntity;

import java.math.BigDecimal;

public record PartidaResponse(
        Long id,
        Long categoriaId,
        String categoriaNombre,
        String nombre,
        BigDecimal importe
) {
    public static PartidaResponse from(PresupuestoPartidaEntity p) {
        return new PartidaResponse(
                p.getId(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombreCategoria() : null,
                p.getNombre(),
                p.getImporte()
        );
    }
}
