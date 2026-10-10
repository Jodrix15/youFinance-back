package com.example.finanzas.dto.inversion;

import com.example.finanzas.model.CuentaEntity;
import com.example.finanzas.model.InversionEntity;

import java.math.BigDecimal;

public record InversionResponse(
        Long id,
        // Activo concreto; null en posiciones sin nombre (usar categoriaNombre).
        String nombre,
        Long categoriaId,
        String categoriaNombre,
        // Color de la categoría; null en categorías antiguas sin color asignado.
        String categoriaColor,
        // Cuenta de inversión; null en posiciones "sin cuenta".
        Long cuentaId,
        String cuentaNombre,
        BigDecimal capitalAportado,
        BigDecimal capitalTotal,
        BigDecimal plusvalia,
        BigDecimal porcentajePlusvalia
) {
    public static InversionResponse from(InversionEntity inversion) {
        CuentaEntity cuenta = inversion.getCuenta();
        return new InversionResponse(
                inversion.getId(),
                inversion.getNombre(),
                inversion.getCategoria() != null ? inversion.getCategoria().getId() : null,
                inversion.getCategoria() != null ? inversion.getCategoria().getNombreCategoria() : null,
                inversion.getCategoria() != null ? inversion.getCategoria().getColor() : null,
                cuenta != null ? cuenta.getId() : null,
                cuenta != null ? cuenta.getNombreCuenta() : null,
                inversion.getCapitalAportado(),
                inversion.getCapitalTotal(),
                inversion.getPlusvalia(),
                inversion.getPorcentajePlusvalia()
        );
    }
}
