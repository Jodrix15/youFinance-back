package com.example.finanzas.dto.categoria;

import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;

public record CategoriaResponse(
        Long id,
        String nombre,
        TipoMovimientoEnum tipo
) {
    public static CategoriaResponse from(CategoriaEntity categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombreCategoria(),
                categoria.getTipo()
        );
    }
}
