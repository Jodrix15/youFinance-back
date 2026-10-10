-- Fecha de inicio de una deuda: primer mes en que se paga su cuota.
-- Opcional: las deudas existentes quedan a NULL y se tratan como ya iniciadas
-- (mismo comportamiento que antes). El widget de gastos fijos no cuenta la
-- cuota en los meses anteriores a esta fecha y, en las anuales, la usa como
-- mes ancla del pago.
ALTER TABLE `deuda`
  ADD COLUMN `fecha_inicio` date DEFAULT NULL AFTER `cuota`;
