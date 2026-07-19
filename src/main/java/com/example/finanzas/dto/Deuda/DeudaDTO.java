package com.example.finanzas.dto.Deuda;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeudaDTO(
        @NotBlank String nombreDeuda,
        @NotNull BigDecimal importe,
        BigDecimal cantidadPagada,
        @NotBlank String acreedor,
        @NotNull FrecuenciaEnum frecuencia,
        @NotNull BigDecimal cuota,
        BigDecimal interes,
        LocalDate fechaVencimiento
) {
    public DeudaDTO {
        if (cantidadPagada == null) {
            cantidadPagada = BigDecimal.ZERO;
        }
    }
}