-- ============================================================================
-- Historial de altas y bajas de gastos recurrentes y suscripciones.
--
-- La V6 guardaba la baja en una única columna `fecha_fin`, pero eso solo
-- registra la última: al reactivar había que limpiarla y pisar también
-- `fecha_primer_pago` y `fecha_ultimo_pago`, con lo que del tramo anterior no
-- quedaba rastro (ni desde cuándo estuvo activo, ni qué pagos tuvo).
--
-- A partir de aquí cada tramo de vida es una fila de `recurrente_periodo`:
-- activar abre un periodo (fecha_fin NULL) y desactivar lo cierra. El gasto
-- sigue siendo uno solo -una tarjeta, un historial de precios-, pero acumula
-- tantos periodos como altas haya tenido. `fecha_primer_pago`, `fecha_fin` y
-- `fecha_ultimo_pago` pasan a derivarse del periodo vigente, así que salen de
-- la tabla padre y la columna que añadió la V6 desaparece.
--
-- Backfill: cada gasto existente arranca con un único periodo. Para los que ya
-- estaban inactivos se usa la `fecha_fin` de la V6 si existe y, si no, se
-- aproxima con el último pago (o el primero si no hubo ninguno). Esa
-- aproximación es una estimación: si alguna baja importa de verdad, hay que
-- corregirla a mano.
--   SELECT * FROM recurrente_periodo WHERE fecha_fin IS NOT NULL;
-- ============================================================================

CREATE TABLE `recurrente_periodo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `gasto_recurrente_id` bigint NOT NULL,
  `fecha_inicio` date NOT NULL,
  -- NULL = periodo abierto, es decir, el gasto está activo ahora mismo.
  `fecha_fin` date DEFAULT NULL,
  -- Último pago registrado dentro de este periodo.
  `fecha_ultimo_pago` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_recurrente_periodo_gasto` (`gasto_recurrente_id`),
  CONSTRAINT `FK_recurrente_periodo_gasto` FOREIGN KEY (`gasto_recurrente_id`)
    REFERENCES `gasto_recurrente` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `recurrente_periodo`
  (`gasto_recurrente_id`, `fecha_inicio`, `fecha_fin`, `fecha_ultimo_pago`)
SELECT g.`id`,
       COALESCE(g.`fecha_primer_pago`, g.`fecha_ultimo_pago`, CURDATE()),
       CASE WHEN g.`is_active` THEN NULL
            ELSE COALESCE(g.`fecha_fin`, g.`fecha_ultimo_pago`,
                          g.`fecha_primer_pago`, CURDATE())
       END,
       g.`fecha_ultimo_pago`
FROM `gasto_recurrente` g;

ALTER TABLE `gasto_recurrente`
  DROP COLUMN `fecha_primer_pago`,
  DROP COLUMN `fecha_ultimo_pago`,
  DROP COLUMN `fecha_fin`;
