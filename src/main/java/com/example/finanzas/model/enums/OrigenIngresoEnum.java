package com.example.finanzas.model.enums;

/**
 * Familia de un ingreso según el esfuerzo que requiere generarlo. Solo aplica
 * a categorías de tipo {@link TipoMovimientoEnum#INGRESO}.
 *
 * <ul>
 *   <li>{@code ACTIVO}: requiere tu tiempo/trabajo (nómina, honorarios, comisiones).</li>
 *   <li>{@code PASIVO}: renta continua tras un trabajo/inversión inicial (alquileres, royalties, licencias).</li>
 *   <li>{@code INVERSION}: rendimiento del capital (dividendos, intereses, rendimientos de fondos).
 *       No confundir con {@link TipoMovimientoEnum#INVERSION}: aquí es dinero que ENTRA como ingreso.</li>
 * </ul>
 */
public enum OrigenIngresoEnum {
    ACTIVO,
    PASIVO,
    INVERSION,
}
