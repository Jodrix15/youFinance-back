package com.example.finanzas.dto.categoria;

import com.example.finanzas.model.enums.OrigenIngresoEnum;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CrearCategoria(
        @NotBlank String nombre,
        @NotNull TipoMovimientoEnum tipo,
        // Familia del ingreso. Obligatoria solo si tipo == INGRESO (se valida en el servicio).
        OrigenIngresoEnum origenIngreso,
        // Color en hex #rrggbb. Opcional: si viene vacío el servicio elige uno
        // de la paleta que no esté ya en uso por otra categoría del mismo tipo.
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color debe tener el formato #rrggbb")
        String color
) {}
