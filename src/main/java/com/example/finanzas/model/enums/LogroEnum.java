package com.example.finanzas.model.enums;

import com.example.finanzas.model.logro.EvaluacionLogro;
import com.example.finanzas.model.logro.LogroContext;

/**
 * Catálogo de logros. Cada logro lleva su metadato (nombre, descripción, icono),
 * la experiencia (XP) que otorga al desbloquearse y su condición de desbloqueo
 * evaluada contra el {@link LogroContext}.
 *
 * <p>De momento todos dan la misma XP; el campo {@code experiencia} está pensado
 * para poder graduar la dificultad en el futuro (más difícil ⇒ más XP).</p>
 */
public enum LogroEnum {

    PRIMERA_CUENTA("Primer paso", "Crea tu primera cuenta", "🏦", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroCuentas() >= 1);
        }
    },
    PRIMER_MOVIMIENTO("En marcha", "Registra tu primer movimiento", "🧾", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroTransacciones() >= 1);
        }
    },
    PRIMERA_INVERSION("Inversor", "Registra tu primera inversión", "📈", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroInversiones() >= 1);
        }
    },
    DIVERSIFICADO("Diversificado", "Invierte en 3 categorías distintas", "🧩", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.categoriasInversion(), 3);
        }
    },
    AHORRADOR("Ahorrador", "Alcanza 1.000 € de patrimonio neto", "🐷", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.patrimonioNeto().doubleValue(), 1000);
        }
    },
    GRAN_AHORRADOR("Gran ahorrador", "Alcanza 10.000 € de patrimonio neto", "💰", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.patrimonioNeto().doubleValue(), 10000);
        }
    },
    LIBRE_DE_DEUDAS("Libre de deudas", "Salda todas tus deudas", "🕊", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroDeudas() >= 1 && c.todasDeudasPagadas());
        }
    },
    MES_EN_VERDE("Mes en verde", "Cierra un mes con más ingresos que gastos", "🟢", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.algunMesEnVerde());
        }
    },
    EN_RACHA("En racha", "Haz crecer tu patrimonio 3 meses seguidos", "🔥", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.progreso(c.rachaCrecimiento(), 3);
        }
    },
    PRESUPUESTADO("Presupuestado", "Crea tu primer presupuesto", "📋", 100) {
        public EvaluacionLogro evaluar(LogroContext c) {
            return EvaluacionLogro.hito(c.numeroPresupuestos() >= 1);
        }
    };

    private final String nombre;
    private final String descripcion;
    private final String icono;
    private final int experiencia;

    LogroEnum(String nombre, String descripcion, String icono, int experiencia) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.experiencia = experiencia;
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

    public int getExperiencia() {
        return experiencia;
    }
}
