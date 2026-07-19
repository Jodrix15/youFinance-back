package com.example.finanzas.dto.gasto;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.model.Gastos.GastoRecurrenteEntity;
import com.example.finanzas.model.Gastos.RecurrentePrecioEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record GastoRecurrenteResponse(
        Long id,
        String nombre,
        Long categoriaId,
        String categoriaNombre,
        TipoPagoEnum tipoPago,
        FrecuenciaEnum frecuencia,
        LocalDate fechaPrimerPago,
        LocalDate fechaUltimoPago,
        LocalDate fechaProximoPago,
        boolean active,
        BigDecimal importeActual,
        List<RecurrentePrecioResponse> historial
) {
    public static GastoRecurrenteResponse from(GastoRecurrenteEntity gasto) {
        List<RecurrentePrecioEntity> precios = gasto.getHistorial() != null
                ? gasto.getHistorial()
                : List.of();

        List<RecurrentePrecioResponse> historial = precios.stream()
                .sorted(Comparator.comparing(RecurrentePrecioEntity::getFechaVariacionImporte))
                .map(RecurrentePrecioResponse::from)
                .toList();

        BigDecimal importeActual = precios.stream()
                .max(Comparator.comparing(RecurrentePrecioEntity::getId))
                .map(RecurrentePrecioEntity::getImporte)
                .orElse(null);

        return new GastoRecurrenteResponse(
                gasto.getId(),
                gasto.getNombre(),
                gasto.getCategoria() != null ? gasto.getCategoria().getId() : null,
                gasto.getCategoria() != null ? gasto.getCategoria().getNombreCategoria() : null,
                gasto.getTipoPago(),
                gasto.getFrecuencia(),
                gasto.getFechaPrimerPago(),
                gasto.getFechaUltimoPago(),
                gasto.getFechaProximoPago(),
                gasto.isActive(),
                importeActual,
                historial
        );
    }
}
