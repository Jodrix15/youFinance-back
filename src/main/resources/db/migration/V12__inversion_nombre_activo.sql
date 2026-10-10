-- Nombre del activo concreto de una posición (p. ej. "MSCI World" dentro de la
-- categoría "Fondos indexados"). Opcional: las posiciones anteriores lo tienen
-- a NULL y la UI muestra el nombre de la categoría en su lugar.
ALTER TABLE `inversion`
  ADD COLUMN `nombre` varchar(120) NULL AFTER `id`;
