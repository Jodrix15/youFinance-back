package com.example.finanzas.dto.presupuesto;

import com.example.finanzas.model.enums.PeriodoPresupuestoEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record PresupuestoDTO(
        @NotBlank(message = "El nombre del presupuesto es obligatorio") String nombre,
        @NotNull(message = "El periodo es obligatorio") PeriodoPresupuestoEnum periodo,
        @NotNull(message = "El año es obligatorio") Integer anio,
        @NotNull(message = "El mes es obligatorio")
        @Min(value = 1, message = "El mes debe estar entre 1 y 12")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12") Integer mes,
        Integer semana,
        @NotNull(message = "La cantidad a presupuestar es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa") BigDecimal cantidadBase,
        boolean descontarGastosFijos,
        @Valid List<PartidaDTO> partidas
) {
    @AssertTrue(message = "Indica la semana del mes (1-5) para un presupuesto semanal")
    public boolean isSemanaValida() {
        if (periodo != PeriodoPresupuestoEnum.SEMANAL) {
            return true;
        }
        return semana != null && semana >= 1 && semana <= 5;
    }
}
