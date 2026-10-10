package com.example.finanzas.dto.inversion;

/**
 * Mueve una posición a otra cuenta de inversión (o la deja sin cuenta con null).
 * Solo cambia la asignación: no genera movimientos de efectivo.
 */
public record AsignarCuentaDTO(Long cuentaId) {}
