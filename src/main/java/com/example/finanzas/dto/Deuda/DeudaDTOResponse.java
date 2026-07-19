package com.example.finanzas.dto.Deuda;

import com.example.finanzas.model.DeudaEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeudaDTOResponse(
    Long id,
    String nombreDeuda,
    BigDecimal importe,
    BigDecimal cantidadPendiente,
    BigDecimal importeTotal,
    BigDecimal cantidadPagada,
    String acreedor,
    BigDecimal cuota,
    FrecuenciaEnum frecuencia,
    BigDecimal interes,
    LocalDate fechaVencimiento
) {
    public static DeudaDTOResponse from(DeudaEntity deuda) {
        return new DeudaDTOResponse(
                deuda.getId(),
                deuda.getNombreDeuda(),
                deuda.getImporte(),
                deuda.getCantidadPendiente(),
                deuda.getImporteTotal(),
                deuda.getCantidadPagada(),
                deuda.getAcreedor(),
                deuda.getCuota(),
                deuda.getFrecuencia(),
                deuda.getInteres(),
                deuda.getFechaVencimiento()
        );
    }
}
