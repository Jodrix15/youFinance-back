package com.example.finanzas.model.enums;

/**
 * Rangos del usuario, del 1 al 10, que se van consiguiendo al acumular
 * experiencia (XP) desbloqueando logros. {@code xpMinimo} es la XP acumulada
 * necesaria para alcanzar el rango. Los saltos crecen con el nivel, de modo que
 * cada rango cuesta más que el anterior.
 */
public enum RangoEnum {

    PLEBEYO("Plebeyo", 0),
    ESCUDERO("Escudero", 100),
    MERCADER("Mercader", 250),
    TERRATENIENTE("Terrateniente", 450),
    CABALLERO("Caballero", 700),
    BARON("Barón", 1000),
    CONDE("Conde", 1400),
    DUQUE("Duque", 1900),
    ARCHIDUQUE("Archiduque", 2500),
    EMPERADOR("Emperador", 3200);

    private final String nombre;
    private final int xpMinimo;

    RangoEnum(String nombre, int xpMinimo) {
        this.nombre = nombre;
        this.xpMinimo = xpMinimo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getXpMinimo() {
        return xpMinimo;
    }

    /** Nivel 1..10. */
    public int getNivel() {
        return ordinal() + 1;
    }

    /** Rango que corresponde a una XP acumulada. */
    public static RangoEnum desdeXp(int xp) {
        RangoEnum actual = PLEBEYO;
        for (RangoEnum r : values()) {
            if (xp >= r.xpMinimo) {
                actual = r;
            }
        }
        return actual;
    }

    /** Siguiente rango, o {@code null} si ya es el máximo. */
    public RangoEnum siguiente() {
        RangoEnum[] valores = values();
        return ordinal() < valores.length - 1 ? valores[ordinal() + 1] : null;
    }
}
