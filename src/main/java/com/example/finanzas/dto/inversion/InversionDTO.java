package com.example.finanzas.dto.inversion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record InversionDTO(
        @NotNull Long categoriaId,
        @NotNull @Positive BigDecimal capitalAportado,
        @NotNull @PositiveOrZero BigDecimal capitalTotal
) {}
