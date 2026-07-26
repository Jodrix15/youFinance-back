-- Familia de la categoría de ingreso (Activo / Pasivo / Inversión).
-- Nullable: solo tiene valor en categorías de tipo INGRESO.
ALTER TABLE `categoria`
  ADD COLUMN `origen_ingreso` enum('ACTIVO','PASIVO','INVERSION') NULL AFTER `tipo_movimiento`;
