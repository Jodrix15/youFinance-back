package com.example.finanzas.model.Gastos;

import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoImporteEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.model.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

    /** Fijo (siempre el mismo importe) o variable (luz, agua...). Solo clasifica. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_importe", nullable = false)
    private TipoImporteEnum tipoImporte = TipoImporteEnum.FIJO;

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

    /** Del más antiguo al más reciente por fecha; a igual fecha, el último registrado. */
    private static final Comparator<RecurrentePrecioEntity> PRECIO_POR_FECHA =
            Comparator.comparing(RecurrentePrecioEntity::getFechaVariacionImporte,
                            Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(RecurrentePrecioEntity::getId,
                            Comparator.nullsLast(Comparator.naturalOrder()));

    /**
     * Precio vigente hoy (el que muestra la tarjeta):
     * - Fijos: por día. Un cambio con fecha futura no se ve hasta ese día.
     * - Variables: por mes. El importe de un mes entra en vigor el día 1 de ese
     *   mes, aunque el cobro caiga más tarde.
     * Nunca es "el último registrado", porque las fechas pueden meterse en
     * cualquier orden. Si todos son futuros, el que llega antes.
     */
    @Transient
    public Optional<RecurrentePrecioEntity> getPrecioVigente() {
        return tipoImporte == TipoImporteEnum.VARIABLE
                ? getPrecioEnMes(YearMonth.now())
                : getPrecioEnFecha(LocalDate.now());
    }

    /**
     * Precio en vigor en un mes cualquiera: el del mes más reciente que no sea
     * posterior a {@code mes}. Así un mes con importe propio usa el suyo y uno
     * sin él arrastra el último conocido. Si todos son posteriores, el primero.
     */
    @Transient
    public Optional<RecurrentePrecioEntity> getPrecioEnMes(YearMonth mes) {
        if (historial == null || historial.isEmpty()) {
            return Optional.empty();
        }
        return historial.stream()
                .filter(p -> p.getFechaVariacionImporte() == null
                        || !YearMonth.from(p.getFechaVariacionImporte()).isAfter(mes))
                .max(PRECIO_POR_FECHA)
                .or(() -> historial.stream().min(PRECIO_POR_FECHA));
    }

    /**
     * Precio en vigor en un día concreto: el último cambio con fecha no
     * posterior a {@code fecha}. Si todos son posteriores, el primero.
     */
    @Transient
    public Optional<RecurrentePrecioEntity> getPrecioEnFecha(LocalDate fecha) {
        if (historial == null || historial.isEmpty()) {
            return Optional.empty();
        }
        return historial.stream()
                .filter(p -> p.getFechaVariacionImporte() == null || !p.getFechaVariacionImporte().isAfter(fecha))
                .max(PRECIO_POR_FECHA)
                .or(() -> historial.stream().min(PRECIO_POR_FECHA));
    }

    /**
     * Día de cobro dentro de un mes: el mismo día del mes que el alta del tramo
     * (día ancla), recortado si el mes es más corto (un 31 cae el 30 o el 28).
     */
    @Transient
    public LocalDate getFechaCobroEnMes(YearMonth mes) {
        LocalDate alta = getFechaPrimerPago();
        int dia = alta != null ? alta.getDayOfMonth() : 1;
        return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
    }

    /**
     * Importe que se cobra en un mes:
     * - Fijos: el precio en vigor el DÍA DE COBRO de ese mes. Un cambio hecho
     *   después del cobro no afecta a ese mes, empieza en el siguiente.
     * - Variables: solo el importe apuntado para ESE mes. Un mes sin importe
     *   cuenta 0: no se arrastra el del mes anterior (un recibo que aún no ha
     *   llegado no se inventa).
     */
    @Transient
    public BigDecimal getImporteEnMes(YearMonth mes) {
        if (tipoImporte == TipoImporteEnum.VARIABLE) {
            return getImporteApuntadoEnMes(mes);
        }
        return getPrecioEnFecha(getFechaCobroEnMes(mes))
                .map(RecurrentePrecioEntity::getImporte)
                .orElse(BigDecimal.ZERO);
    }

    /** Importe registrado exactamente en ese mes (el último, si hubiera varios); 0 si no hay. */
    @Transient
    public BigDecimal getImporteApuntadoEnMes(YearMonth mes) {
        if (historial == null) {
            return BigDecimal.ZERO;
        }
        return historial.stream()
                .filter(p -> p.getFechaVariacionImporte() != null
                        && YearMonth.from(p.getFechaVariacionImporte()).equals(mes))
                .max(PRECIO_POR_FECHA)
                .map(RecurrentePrecioEntity::getImporte)
                .orElse(BigDecimal.ZERO);
    }

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
