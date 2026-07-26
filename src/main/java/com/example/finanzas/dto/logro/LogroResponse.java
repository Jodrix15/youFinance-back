package com.example.finanzas.dto.logro;

import java.time.Instant;

public record LogroResponse(
        String codigo,
        String nombre,
        String descripcion,
        String icono,
        boolean desbloqueado,
        Instant fechaDesbloqueo,
        Double progresoActual,
        Double progresoObjetivo,
        // true solo en la respuesta en que se desbloquea (para notificar en el front).
        boolean nuevo
) {
}
