package com.example.finanzas.model.enums;

import com.example.finanzas.model.logro.EvaluacionLogro;
import com.example.finanzas.model.logro.LogroContext;

/**
 * Catálogo de logros. Cada logro lleva su metadato (nombre, descripción, icono)
 * y su condición de desbloqueo evaluada contra el {@link LogroContext}.
 */
public enum LogroEnum {

    PRIMERA_CUENTA("Primer paso", "Crea tu primera cuenta", "🏦") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroCuentas() >= 1);
        }
    },
    PRIMER_MOVIMIENTO("En marcha", "Registra tu primer movimiento", "🧾") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroTransacciones() >= 1);
        }
    },
    PRIMERA_INVERSION("Inversor", "Registra tu primera inversión", "📈") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroInversiones() >= 1);
        }
    },
    DIVERSIFICADO("Diversificado", "Invierte en 3 categorías distintas", "🧩") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.categoriasInversion(), 3);
        }
    },
    AHORRADOR("Ahorrador", "Alcanza 1.000 € de patrimonio neto", "🐷") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.patrimonioNeto().doubleValue(), 1000);
        }
    },
    GRAN_AHORRADOR("Gran ahorrador", "Alcanza 10.000 € de patrimonio neto", "💰") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.patrimonioNeto().doubleValue(), 10000);
        }
    },
    LIBRE_DE_DEUDAS("Libre de deudas", "Salda todas tus deudas", "🕊") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroDeudas() >= 1 && c.todasDeudasPagadas());
        }
    },
    MES_EN_VERDE("Mes en verde", "Cierra un mes con más ingresos que gastos", "🟢") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.algunMesEnVerde());
        }
    },
    EN_RACHA("En racha", "Haz crecer tu patrimonio 3 meses seguidos", "🔥") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.rachaCrecimiento(), 3);
        }
    },
    PRESUPUESTADO("Presupuestado", "Crea tu primer presupuesto", "📋") {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroPresupuestos() >= 1);
        }
    };

    private final String nombre;
    private final String descripcion;
    private final String icono;

    LogroEnum(String nombre, String descripcion, String icono) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
    }

    public abstract EvaluacionLogro evaluar(LogroContext contexto);

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getIcono() {
        return icono;
    }
}
