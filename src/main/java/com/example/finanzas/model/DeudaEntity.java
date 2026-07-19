package com.example.finanzas.model;

import com.example.finanzas.model.enums.FrecuenciaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "deuda")
@Getter
@Setter
@NoArgsConstructor
public class DeudaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre", nullable=false)
    private String nombreDeuda;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private BigDecimal importe;

    @Column(nullable = false)
    private BigDecimal cantidadPagada = BigDecimal.ZERO;

    @Column(nullable = false)
    private String acreedor;

    private BigDecimal interes;

    @Enumerated(EnumType.STRING)
    private FrecuenciaEnum frecuencia;

    private BigDecimal cuota = BigDecimal.ZERO;

    private LocalDate fechaVencimiento;

    // Valores derivados: se calculan a partir de importe/interes/cantidadPagada, no se persisten
    // para evitar quedar desincronizados (mismo patron que InversionEntity.getPlusvalia).

    @Transient
    public BigDecimal getImporteTotal() {
        if (importe == null) {
            return null;
        }
        if (interes == null) {
            return importe;
        }
        BigDecimal intereses = importe.multiply(interes)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return importe.add(intereses);
    }

    @Transient
    public BigDecimal getCantidadPendiente() {
        BigDecimal total = getImporteTotal();
        if (total == null || cantidadPagada == null) {
            return null;
        }
        return total.subtract(cantidadPagada);
    }
}
