package com.example.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Foto mensual del patrimonio de un usuario. Se guarda un registro por
 * usuario y mes (identificado por el primer día del mes en {@code mes}),
 * de modo que la curva de evolución sea histórica y real en vez de
 * reconstruida a partir de los movimientos.
 */
@Entity
@Table(
        name = "patrimonio_snapshot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_snapshot_user_mes",
                columnNames = {"user_id", "mes"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatrimonioSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** Primer día del mes al que corresponde la foto (ej. 2026-07-01). */
    @Column(name = "mes", nullable = false)
    private LocalDate mes;

    @Column(nullable = false)
    private BigDecimal patrimonioNeto;

    @Column(nullable = false)
    private BigDecimal totalCuentas;

    @Column(nullable = false)
    private BigDecimal totalInversiones;

    @Column(nullable = false)
    private BigDecimal totalDeudas;

    /** Última vez que se recalculó la foto de este mes. */
    @Column(nullable = false)
    private Instant actualizadoEn;
}
