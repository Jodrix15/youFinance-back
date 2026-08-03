-- ============================================================================
-- El último pago pasa a ser derivado, no almacenado.
--
-- `recurrente_periodo.fecha_ultimo_pago` nació para que alguien fuese marcando
-- los pagos uno a uno, pero ese endpoint no lo llamaba nadie: la columna se
-- quedaba a NULL para siempre y, como el próximo pago se calcula a partir de
-- ella, quedaba congelado en la fecha de alta.
--
-- No hace falta guardarlo: los cobros caen siempre el mismo día del mes (o del
-- año) que el alta, así que el último pago se deduce del día ancla y de la
-- fecha de referencia -hoy si el periodo sigue abierto, la fecha de baja si ya
-- está cerrado-. Ahora lo calcula RecurrentePeriodoEntity.getFechaUltimoPago().
-- ============================================================================

ALTER TABLE `recurrente_periodo`
  DROP COLUMN `fecha_ultimo_pago`;
