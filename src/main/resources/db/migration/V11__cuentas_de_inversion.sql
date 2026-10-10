-- ============================================================================
-- Cuentas de inversión.
--
-- Las plataformas de inversión (Mintos, un broker…) se guardan en la misma
-- tabla `cuenta` que las cuentas corrientes, diferenciadas por `tipo`. Así las
-- aportaciones y retiradas son transferencias normales entre cuentas y el
-- saldo de una cuenta de inversión es su efectivo sin invertir.
--
-- Las filas existentes pasan a ser CORRIENTE (el default).
-- ============================================================================

ALTER TABLE `cuenta`
  ADD COLUMN `tipo` enum('CORRIENTE','INVERSION') NOT NULL DEFAULT 'CORRIENTE' AFTER `nombre`,
  ADD COLUMN `plataforma` enum('BROKER','FONDOS','P2P','ROBOADVISOR','CRIPTO','OTRO') NULL AFTER `tipo`,
  ADD COLUMN `color` varchar(7) NULL AFTER `plataforma`,
  ADD COLUMN `cuenta_vinculada_id` bigint NULL AFTER `color`;

ALTER TABLE `cuenta`
  ADD KEY `idx_cuenta_vinculada` (`cuenta_vinculada_id`),
  ADD CONSTRAINT `fk_cuenta_vinculada`
      FOREIGN KEY (`cuenta_vinculada_id`) REFERENCES `cuenta` (`id`) ON DELETE SET NULL;

-- Cada posición puede pertenecer a una cuenta de inversión. Nullable: las
-- inversiones anteriores a este cambio quedan "sin cuenta" hasta que el
-- usuario las asigne.
ALTER TABLE `inversion`
  ADD COLUMN `cuenta_id` bigint NULL AFTER `categoria_id`,
  ADD KEY `idx_inversion_cuenta` (`cuenta_id`),
  ADD CONSTRAINT `fk_inversion_cuenta`
      FOREIGN KEY (`cuenta_id`) REFERENCES `cuenta` (`id`);
