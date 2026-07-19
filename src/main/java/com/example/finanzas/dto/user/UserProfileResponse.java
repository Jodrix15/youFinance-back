package com.example.finanzas.dto.user;

import com.example.finanzas.model.enums.MonedaEnum;
import lombok.Builder;
import lombok.Data;

/**
 * Perfil del usuario autenticado. El campo {@code token} solo se rellena cuando
 * una operación invalida el JWT actual (p.ej. cambio de username); en el resto
 * de casos viaja como null.
 */
@Data
@Builder
public class UserProfileResponse {
    private String username;
    private String email;
    private String role;
    private String fotoPerfil;
    private MonedaEnum moneda;
    private String idioma;
    private String token;
}
