package com.example.finanzas.dto.cuenta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AporteInversionDTO(
        @NotNull @Positive BigDecimal importe
) {}
