-- ============================================================================
-- 1) El saldo de la cuenta pasa a ser derivado.
--
-- Hasta ahora `cuenta.importe` era un saldo acumulado que se mutaba en cada
-- alta/edición/borrado de transacción. Cualquier fallo en una de esas
-- escrituras lo desincronizaba para siempre (y de hecho la edición sumaba el
-- importe nuevo sin restar el anterior). A partir de aquí solo se guarda el
-- saldo de partida y el actual se calcula: saldo = saldo_inicial + SUM(importe).
--
-- Backfill: saldo_inicial = importe_actual - suma de movimientos. Así el saldo
-- que ve el usuario no cambia de golpe tras la migración. OJO: si una cuenta
-- arrastraba el bug de la doble contabilización, el desvío queda ahora dentro
-- de `saldo_inicial` y hay que corregirlo a mano editando la cuenta.
-- Para revisarlas:
--   SELECT id, nombre, saldo_inicial FROM cuenta WHERE saldo_inicial <> 0;
-- ============================================================================

ALTER TABLE `cuenta`
  ADD COLUMN `saldo_inicial` decimal(38,2) NOT NULL DEFAULT 0 AFTER `nombre`;

UPDATE `cuenta` c
SET c.`saldo_inicial` = c.`importe` - COALESCE(
        (SELECT SUM(t.`importe`) FROM `transaccion` t WHERE t.`cuenta_id` = c.`id`), 0);

ALTER TABLE `cuenta`
  DROP COLUMN `importe`,
  ALTER COLUMN `saldo_inicial` DROP DEFAULT;

-- ============================================================================
-- 2) Nuevo tipo de movimiento: TRANSFERENCIA.
--
-- Se modela como doble apunte: dos filas con el mismo `transferencia_id`, una
-- negativa en la cuenta origen y otra positiva en la de destino. De ese modo el
-- saldo de cada cuenta sigue siendo la simple suma de sus movimientos y el
-- traspaso no altera el patrimonio total.
--
-- `categoria_id` pasa a ser nullable: un traspaso entre cuentas propias no se
-- categoriza (no es ni ingreso ni gasto).
-- `cuenta_contrapartida_id` guarda la otra cuenta implicada, desnormalizada,
-- para poder pintar "Transferencia a Ahorro" sin un self-join.
-- ============================================================================

ALTER TABLE `transaccion`
  MODIFY COLUMN `tipo_movimiento` enum('GASTO','INGRESO','INVERSION','TRANSFERENCIA') DEFAULT NULL,
  MODIFY COLUMN `categoria_id` bigint NULL,
  ADD COLUMN `transferencia_id` varchar(36) NULL AFTER `tipo_movimiento`,
  ADD COLUMN `cuenta_contrapartida_id` bigint NULL AFTER `cuenta_id`;

ALTER TABLE `transaccion`
  ADD KEY `idx_transaccion_transferencia` (`transferencia_id`),
  ADD KEY `idx_transaccion_contrapartida` (`cuenta_contrapartida_id`),
  ADD CONSTRAINT `fk_transaccion_cuenta_contrapartida`
      FOREIGN KEY (`cuenta_contrapartida_id`) REFERENCES `cuenta` (`id`);
