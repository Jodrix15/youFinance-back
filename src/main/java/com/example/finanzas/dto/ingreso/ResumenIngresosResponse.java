package com.example.finanzas.dto.ingreso;

import java.math.BigDecimal;
import java.util.List;

/** Total de ingresos del periodo y su reparto por familia. */
public record ResumenIngresosResponse(
        BigDecimal total,
        List<IngresoFamiliaResponse> familias
) {
}
