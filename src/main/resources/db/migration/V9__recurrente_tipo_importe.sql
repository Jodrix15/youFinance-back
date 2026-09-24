-- ============================================================================
-- Recurrentes fijos y variables.
--
-- `tipo_importe` distingue los gastos que cobran siempre lo mismo (FIJO:
-- alquiler, gimnasio...) de los que cambian en cada cobro (VARIABLE: luz,
-- agua, gas...). Es solo una clasificación: el importe vigente sigue siendo el
-- último del historial de precios en ambos casos.
--
-- Backfill: todo lo existente queda como FIJO (el DEFAULT); los que sean
-- variables se reclasifican editándolos desde la pantalla de Recurrentes.
-- ============================================================================

ALTER TABLE `gasto_recurrente`
  ADD COLUMN `tipo_importe` enum('FIJO','VARIABLE') NOT NULL DEFAULT 'FIJO' AFTER `frecuencia`;
