package com.example.finanzas.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Actualización de datos de perfil. La foto viaja como data URL base64
 * (o null/"" para eliminarla). El email es opcional.
 */
@Data
public class UpdateProfileRequest {

    @NotBlank
    @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres")
    private String username;

    @Email(message = "El email no es válido")
    private String email;

    private String fotoPerfil;
}
