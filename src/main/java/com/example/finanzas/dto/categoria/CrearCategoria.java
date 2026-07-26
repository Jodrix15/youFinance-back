package com.example.finanzas.dto.categoria;

import com.example.finanzas.model.enums.OrigenIngresoEnum;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearCategoria(
        @NotBlank String nombre,
        @NotNull TipoMovimientoEnum tipo,
        // Familia del ingreso. Obligatoria solo si tipo == INGRESO (se valida en el servicio).
        OrigenIngresoEnum origenIngreso
) {}
