package com.example.finanzas.model.enums;

/**
 * Naturaleza de una cuenta.
 *
 * <ul>
 *   <li>{@code CORRIENTE}: cuenta bancaria de uso diario (nómina, ahorro…).</li>
 *   <li>{@code INVERSION}: plataforma donde viven inversiones (Mintos, un broker…).
 *       Su saldo es el efectivo sin invertir; las posiciones se guardan aparte
 *       en {@code inversion} y apuntan a esta cuenta.</li>
 * </ul>
 */
public enum TipoCuentaEnum {
    CORRIENTE,
    INVERSION
}
