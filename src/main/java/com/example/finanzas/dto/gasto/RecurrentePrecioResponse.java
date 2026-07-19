package com.example.finanzas.dto.gasto;

import com.example.finanzas.model.Gastos.RecurrentePrecioEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurrentePrecioResponse(
        Long id,
        LocalDate fechaVariacionImporte,
        BigDecimal importe
) {
    public static RecurrentePrecioResponse from(RecurrentePrecioEntity precio) {
        return new RecurrentePrecioResponse(
                precio.getId(),
                precio.getFechaVariacionImporte(),
                precio.getImporte()
        );
    }
}
