package com.example.finanzas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Devuelve el 403 como JSON con el motivo REAL de la excepción, en vez del
 * genérico "Forbidden". Así el frontend puede distinguir entre un fallo de CSRF
 * (p. ej. "Invalid CSRF token…") y una denegación por propiedad de los datos
 * (p. ej. "No tienes acceso a esta cuenta"). Se usa tanto para el CsrfFilter
 * como para las AccessDeniedException lanzadas en los servicios.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String message = ex.getMessage() != null ? ex.getMessage() : "Acceso denegado";
        response.getWriter().write(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    /** Escapa comillas, barras y saltos de línea para no romper el JSON. */
    private String escapeJson(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
