package com.example.finanzas.dto.inversion;

import com.example.finanzas.model.InversionEntity;

import java.math.BigDecimal;

public record InversionResponse(
        Long id,
        Long categoriaId,
        String categoriaNombre,
        BigDecimal capitalAportado,
        BigDecimal capitalTotal,
        BigDecimal plusvalia,
        BigDecimal porcentajePlusvalia
) {
    public static InversionResponse from(InversionEntity inversion) {
        return new InversionResponse(
                inversion.getId(),
                inversion.getCategoria() != null ? inversion.getCategoria().getId() : null,
                inversion.getCategoria() != null ? inversion.getCategoria().getNombreCategoria() : null,
                inversion.getCapitalAportado(),
                inversion.getCapitalTotal(),
                inversion.getPlusvalia(),
                inversion.getPorcentajePlusvalia()
        );
    }
}