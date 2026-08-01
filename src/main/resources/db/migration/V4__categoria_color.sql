-- Color de la categoría (hex #rrggbb), usado en gráficos y listados.
-- Nullable en la tabla porque el usuario no está obligado a elegirlo, pero el
-- backend siempre guarda uno: si no lo indica, asigna el primer color libre de
-- la paleta dentro de su mismo tipo de movimiento.
ALTER TABLE `categoria`
  ADD COLUMN `color` varchar(7) NULL AFTER `origen_ingreso`;

-- Backfill de las categorías existentes: reparte la paleta por orden de id
-- dentro de cada (usuario, tipo), de forma que dos categorías del mismo grupo
-- no repitan color mientras haya colores libres.
UPDATE `categoria` c
JOIN (
  SELECT `id`,
         ELT(1 + MOD(ROW_NUMBER() OVER (PARTITION BY `user_id`, `tipo_movimiento`
                                        ORDER BY `id`) - 1, 7),
             '#2f81f7', '#1d9e75', '#d29922', '#8b7ec8',
             '#d85a30', '#d4537e', '#6e7681') AS `color`
  FROM `categoria`
) x ON x.`id` = c.`id`
SET c.`color` = x.`color`;
