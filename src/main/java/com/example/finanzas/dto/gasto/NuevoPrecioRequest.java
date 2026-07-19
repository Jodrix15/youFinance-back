package com.example.finanzas.dto.gasto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class NuevoPrecioRequest {

    @NotNull
    @Positive
    private BigDecimal importe;

    @NotNull
    private LocalDate fechaVariacionImporte;
}
