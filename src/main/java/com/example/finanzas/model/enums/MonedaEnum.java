package com.example.finanzas.model.enums;

/**
 * Monedas soportadas por la aplicación. El símbolo se usa en el front para
 * formatear importes cuando el formateo por moneda esté activo.
 */
public enum MonedaEnum {
    EUR("€"),
    USD("$"),
    GBP("£");

    private final String simbolo;

    MonedaEnum(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
