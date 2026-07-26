package com.example.finanzas.dto.Deuda;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeudaDTO(
        @NotBlank(message = "El nombre de la deuda es obligatorio") String nombreDeuda,
        @NotNull(message = "El importe es obligatorio")
        @PositiveOrZero(message = "El importe no puede ser negativo") BigDecimal importe,
        @PositiveOrZero(message = "La cantidad pagada no puede ser negativa") BigDecimal cantidadPagada,
        @NotBlank(message = "El acreedor es obligatorio") String acreedor,
        @NotNull(message = "La frecuencia es obligatoria") FrecuenciaEnum frecuencia,
        @NotNull(message = "La cuota es obligatoria")
        @PositiveOrZero(message = "La cuota no puede ser negativa") BigDecimal cuota,
        @PositiveOrZero(message = "El interés no puede ser negativo") BigDecimal interes,
        LocalDate fechaVencimiento
) {
    public DeudaDTO {
        if (cantidadPagada == null) {
            cantidadPagada = BigDecimal.ZERO;
        }
    }

    @AssertTrue(message = "La cantidad pagada no puede superar el importe de la deuda")
    public boolean isCantidadPagadaValida() {
        if (importe == null || cantidadPagada == null) {
            return true; // otras validaciones ya cubren los nulos
        }
        return cantidadPagada.compareTo(importe) <= 0;
    }
}
