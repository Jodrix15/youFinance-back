-- ============================================================================
-- Fecha de baja de gastos recurrentes y suscripciones.
--
-- Hasta ahora desactivar un recurrente solo ponía `is_active` a 0, sin dejar
-- rastro de cuándo se dio de baja. `fecha_fin` guarda ese momento: la escribe
-- el backend al pasar de activo a inactivo y la limpia al reactivar (que
-- además resetea fecha_primer_pago a hoy y fecha_ultimo_pago a NULL, para que
-- el próximo pago se recalcule desde cero).
--
-- No se hace backfill: de los inactivos actuales no sabemos la fecha real de
-- baja e inventarla (p. ej. a partir del último pago) daría un dato falso.
-- Se quedan a NULL y la interfaz simplemente no muestra la línea de baja.
-- ============================================================================

ALTER TABLE `gasto_recurrente`
  ADD COLUMN `fecha_fin` date DEFAULT NULL AFTER `fecha_ultimo_pago`;
