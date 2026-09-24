-- ============================================================================
-- El precio del alta no puede ser anterior al primer pago.
--
-- Hasta ahora el primer precio de un recurrente se guardaba con la fecha del
-- día en que se creaba. Si el primer pago era de un mes futuro (alta hoy, 24
-- de septiembre, con primer pago en octubre), en los variables ese importe
-- caía en septiembre: aparecía en «Importe del mes», en la gráfica y en el
-- gasto anual en el mes equivocado. El backend ya guarda
-- max(hoy, fecha del primer pago); esto corrige los que ya existen.
--
-- Solo se toca el PRIMER precio de cada gasto (el del alta, menor id) y solo si
-- quedó antes del inicio de su primer periodo. El resto del historial no cambia.
-- Las subconsultas van envueltas en una tabla derivada extra para que MySQL las
-- materialice y deje actualizar la misma tabla (error 1093).
-- ============================================================================

UPDATE `recurrente_precio` p
JOIN (
    SELECT * FROM (
        SELECT `gasto_recurrente_id`, MIN(`id`) AS `id`
        FROM `recurrente_precio`
        GROUP BY `gasto_recurrente_id`
    ) x
) primero ON primero.`id` = p.`id`
JOIN (
    SELECT `gasto_recurrente_id`, MIN(`fecha_inicio`) AS `inicio`
    FROM `recurrente_periodo`
    GROUP BY `gasto_recurrente_id`
) periodo ON periodo.`gasto_recurrente_id` = p.`gasto_recurrente_id`
SET p.`fecha_variacion_importe` = periodo.`inicio`
WHERE p.`fecha_variacion_importe` < periodo.`inicio`;
