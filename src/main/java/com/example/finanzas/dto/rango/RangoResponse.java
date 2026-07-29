package com.example.finanzas.dto.rango;

/**
 * Rango actual del usuario y su progreso hacia el siguiente.
 *
 * @param nivel             1..10
 * @param nombre            nombre del rango (p. ej. "Estratega")
 * @param experienciaTotal  XP acumulada por los logros desbloqueados
 * @param xpRangoActual     XP con la que empieza el rango actual
 * @param xpSiguiente       XP necesaria para el siguiente rango (null si es el máximo)
 * @param progreso          0..100, porcentaje avanzado dentro del rango actual
 */
public record RangoResponse(
        int nivel,
        String nombre,
        int experienciaTotal,
        int xpRangoActual,
        Integer xpSiguiente,
        int progreso
) {
}
