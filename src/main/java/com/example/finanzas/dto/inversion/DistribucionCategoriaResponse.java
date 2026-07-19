package com.example.finanzas.dto.inversion;

import java.math.BigDecimal;

public record DistribucionCategoriaResponse(
        Long categoriaId,
        String categoriaNombre,
        BigDecimal capitalTotal,
        BigDecimal porcentaje
) {
}