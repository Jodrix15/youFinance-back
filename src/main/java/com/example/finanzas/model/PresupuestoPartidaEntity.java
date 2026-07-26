package com.example.finanzas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Partida de un presupuesto. Puede estar ligada a una categoría del usuario
 * (para poder comparar con el gasto real) o ser una línea libre con nombre
 * propio (categoria == null y nombre != null).
 */
@Entity
@Table(name = "presupuesto_partida")
@Getter
@Setter
@NoArgsConstructor
public class PresupuestoPartidaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "presupuesto_id", nullable = false)
    private PresupuestoEntity presupuesto;

    /** Categoría asociada (opcional). Si es null, se trata de una línea libre. */
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    /** Nombre de la partida (obligatorio en líneas libres; opcional si hay categoría). */
    private String nombre;

    @Column(nullable = false)
    private BigDecimal importe = BigDecimal.ZERO;
}
