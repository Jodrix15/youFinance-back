package com.example.finanzas.dto.categoria;

import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearCategoria(
        @NotBlank String nombre,
        @NotNull TipoMovimientoEnum tipo
) {}
