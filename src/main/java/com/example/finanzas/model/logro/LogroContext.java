package com.example.finanzas.model.logro;

import java.math.BigDecimal;

/** Datos agregados del usuario contra los que se evalúan los logros. */
public record LogroContext(
        int numeroCuentas,
        long numeroTransacciones,
        int numeroInversiones,
        int categoriasInversion,
        int numeroDeudas,
        boolean todasDeudasPagadas,
        BigDecimal patrimonioNeto,
        int rachaCrecimiento,
        boolean algunMesEnVerde,
        int numeroPresupuestos
) {
}
