package com.example.finanzas.model.Gastos;

import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.model.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "gasto_recurrente")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GastoRecurrenteEntity {

    /** Del más antiguo al más reciente. Un periodo recién creado (sin id) es el último. */
    private static final Comparator<RecurrentePeriodoEntity> POR_ANTIGUEDAD =
            Comparator.comparing(RecurrentePeriodoEntity::getFechaInicio,
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(RecurrentePeriodoEntity::getId,
                            Comparator.nullsLast(Comparator.naturalOrder()));

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Enumerated(EnumType.STRING)
    private TipoPagoEnum tipoPago;

    @Enumerated(EnumType.STRING)
    private FrecuenciaEnum frecuencia;

    @OneToMany(mappedBy = "gastoRecurrente", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<RecurrentePrecioEntity> historial = new ArrayList<>();

    /**
     * Tramos de alta/baja, del más antiguo al más reciente. Reactivar añade uno
     * nuevo en vez de pisar el anterior, así que aquí queda toda la vida del gasto.
     */
    @OneToMany(mappedBy = "gastoRecurrente", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<RecurrentePeriodoEntity> periodos = new ArrayList<>();

    /** Redundante con {@link #getPeriodoActual()}; lo mantiene sincronizado el servicio. */
    @Column(nullable = false)
    private boolean isActive;

    /** Periodos ordenados cronológicamente. */
    @Transient
    public List<RecurrentePeriodoEntity> getPeriodosOrdenados() {
        return periodos.stream().sorted(POR_ANTIGUEDAD).toList();
    }

    /** El periodo más reciente, esté abierto o cerrado. */
    @Transient
    public Optional<RecurrentePeriodoEntity> getUltimoPeriodo() {
        return periodos.stream().max(POR_ANTIGUEDAD);
    }

    /** El periodo vigente. Vacío si el gasto está dado de baja. */
    @Transient
    public Optional<RecurrentePeriodoEntity> getPeriodoActual() {
        return getUltimoPeriodo().filter(RecurrentePeriodoEntity::isAbierto);
    }

    // Derivado: alta del tramo vigente (o del último, si está dado de baja).
    @Transient
    public LocalDate getFechaPrimerPago() {
        return getUltimoPeriodo().map(RecurrentePeriodoEntity::getFechaInicio).orElse(null);
    }

    // Derivado: último cobro vencido en ese mismo tramo.
    @Transient
    public LocalDate getFechaUltimoPago() {
        return getUltimoPeriodo().map(RecurrentePeriodoEntity::getFechaUltimoPago).orElse(null);
    }

    // Derivado: fecha de baja del último tramo. Null mientras esté activo.
    @Transient
    public LocalDate getFechaFin() {
        return getUltimoPeriodo().map(RecurrentePeriodoEntity::getFechaFin).orElse(null);
    }

    // Derivado: el próximo pago es el último + un periodo; si aún no ha vencido
    // ninguno, es el primer pago del tramo. Null si el gasto está dado de baja:
    // un periodo cerrado ya no genera cobros.
    @Transient
    public LocalDate getFechaProximoPago() {
        if (frecuencia == null || getPeriodoActual().isEmpty()) {
            return null;
        }
        LocalDate ultimoPago = getFechaUltimoPago();
        if (ultimoPago == null) {
            return getFechaPrimerPago();
        }
        return switch (frecuencia) {
            case MENSUAL -> ultimoPago.plusMonths(1);
            case ANUAL -> ultimoPago.plusYears(1);
        };
    }
}
