package com.example.finanzas.dto.user;

import com.example.finanzas.model.enums.MonedaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePreferencesRequest {

    @NotNull(message = "La moneda es obligatoria")
    private MonedaEnum moneda;

    @NotBlank(message = "El idioma es obligatorio")
    private String idioma;
}
