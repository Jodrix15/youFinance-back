# Migraciones Flyway

Convención de nombres: `V<versión>__<descripción>.sql` (doble guion bajo).
Ejemplos: `V1__init.sql`, `V2__add_feedback_estado.sql`.

Reglas:
- Cada archivo se aplica **una sola vez** por base de datos (Flyway lo registra en
  la tabla `flyway_schema_history`).
- **Nunca** edites una migración ya aplicada en algún entorno: crea una nueva.
- Solo DDL / cambios de esquema (y datos de referencia si hace falta), nunca los
  datos propios de cada entorno.

El `V1__init.sql` es el esquema completo actual (baseline). En las BDs que ya
existen no se ejecuta (se marcan como baseline); en una BD vacía crea todo.
