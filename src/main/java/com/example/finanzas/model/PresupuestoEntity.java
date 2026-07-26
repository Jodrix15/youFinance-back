package com.example.finanzas.model;

import com.example.finanzas.model.enums.PeriodoPresupuestoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presupuesto")
@Getter
@Setter
@NoArgsConstructor
public class PresupuestoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodoPresupuestoEnum periodo;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    /** Semana del mes (1..5). Solo se usa cuando el periodo es SEMANAL. */
    private Integer semana;

    /** Cantidad que introduce el usuario para presupuestar (antes de restar gastos fijos). */
    @Column(nullable = false)
    private BigDecimal cantidadBase = BigDecimal.ZERO;

    /**
     * Si es true, el dinero disponible del periodo se calcula restando los gastos
     * fijos mensuales a {@link #cantidadBase} (el cálculo del importe fijo se hace
     * en el front, reutilizando el widget de gastos fijos).
     */
    @Column(nullable = false)
    private boolean descontarGastosFijos = true;

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PresupuestoPartidaEntity> partidas = new ArrayList<>();

    /** Añade una partida y mantiene la relación bidireccional consistente. */
    public void addPartida(PresupuestoPartidaEntity partida) {
        partida.setPresupuesto(this);
        this.partidas.add(partida);
    }

    /** Vacía las partidas (para reemplazarlas al actualizar). */
    public void clearPartidas() {
        this.partidas.clear();
    }
}
