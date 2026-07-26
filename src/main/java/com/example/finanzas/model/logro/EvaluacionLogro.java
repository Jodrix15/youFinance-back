package com.example.finanzas.model.logro;

/** Resultado de evaluar un logro: si está cumplido y, opcionalmente, su progreso. */
public record EvaluacionLogro(boolean cumplido, Double actual, Double objetivo) {

    /** Logro de tipo hito (cumplido o no, sin barra de progreso). */
    public static EvaluacionLogro hito(boolean cumplido) {
        return new EvaluacionLogro(cumplido, null, null);
    }

    /** Logro con progreso: cumplido cuando actual >= objetivo. */
    public static EvaluacionLogro progreso(double actual, double objetivo) {
        return new EvaluacionLogro(actual >= objetivo, actual, objetivo);
    }
}
