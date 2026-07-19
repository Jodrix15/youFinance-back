package com.example.finanzas.dto.gasto;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActualizarGasto(
        @NotNull String nombre,
        @NotNull Long categoriaId,
        @NotNull TipoPagoEnum tipoPago,
        @NotNull FrecuenciaEnum frecuencia,
        @NotNull LocalDate fechaPrimerPago,
        @NotNull LocalDate fechaUltimoPago,
        boolean active
) {}
