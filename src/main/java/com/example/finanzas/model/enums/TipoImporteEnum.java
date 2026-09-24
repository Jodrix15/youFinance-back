package com.example.finanzas.model.enums;

/**
 * Naturaleza del importe de un gasto recurrente.
 * FIJO: siempre cobra lo mismo (alquiler, gimnasio...).
 * VARIABLE: cambia de un cobro a otro (luz, agua, gas...).
 * Solo clasifica: en ambos casos el importe vigente es el último del historial.
 */
public enum TipoImporteEnum {
    FIJO,
    VARIABLE
}
