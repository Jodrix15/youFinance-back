package com.example.finanzas.model.enums;

public enum TipoMovimientoEnum {
    GASTO,
    INGRESO,
    INVERSION,
    /**
     * Traspaso entre dos cuentas propias. No es ni ingreso ni gasto: se guarda
     * como un par de apuntes (salida negativa en origen, entrada positiva en
     * destino) ligados por el mismo {@code transferenciaId}, de forma que la
     * suma sobre el conjunto de cuentas es cero.
     */
    TRANSFERENCIA;

    /** Tipos que restan del saldo de su cuenta. */
    public boolean restaDelSaldo() {
        return this == GASTO || this == INVERSION;
    }
}
