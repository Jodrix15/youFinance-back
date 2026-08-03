package com.example.finanzas.model.Gastos;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Tramo de vida de un gasto recurrente: se abre al activarlo y se cierra al
 * darlo de baja. Un mismo gasto puede tener varios a lo largo del tiempo, así
 * que reactivar no pisa la información del tramo anterior.
 */
@Entity
@Table(name = "recurrente_periodo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecurrentePeriodoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gasto_recurrente_id", nullable = false)
    @JsonIgnore
    private GastoRecurrenteEntity gastoRecurrente;

    /** Fecha de alta del tramo; marca el día ancla en el que caen los cobros. */
    @Column(nullable = false)
    private LocalDate fechaInicio;

    /** Fecha de baja. Null mientras el periodo siga abierto (gasto activo). */
    private LocalDate fechaFin;

    @Transient
    public boolean isAbierto() {
        return fechaFin == null;
    }

    /** Hasta cuándo cuentan los cobros: hoy si sigue abierto, la baja si no. */
    @Transient
    public LocalDate getFechaReferencia() {
        return fechaFin != null ? fechaFin : LocalDate.now();
    }

    /**
     * Último cobro del tramo. No se almacena: los pagos caen siempre el mismo
     * día que el alta, así que basta con contar cuántos periodos completos han
     * pasado hasta la fecha de referencia. Null si aún no ha vencido ninguno,
     * es decir, si se dio de baja antes del primer pago.
     */
    @Transient
    public LocalDate getFechaUltimoPago() {
        FrecuenciaEnum frecuencia = gastoRecurrente != null ? gastoRecurrente.getFrecuencia() : null;
        if (frecuencia == null || fechaInicio == null) {
            return null;
        }
        LocalDate referencia = getFechaReferencia();
        if (referencia.isBefore(fechaInicio)) {
            return null;
        }
        return switch (frecuencia) {
            case MENSUAL -> fechaInicio.plusMonths(ChronoUnit.MONTHS.between(fechaInicio, referencia));
            case ANUAL -> fechaInicio.plusYears(ChronoUnit.YEARS.between(fechaInicio, referencia));
        };
    }
}
