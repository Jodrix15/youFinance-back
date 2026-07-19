package com.example.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "inversion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InversionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "capital_aportado", nullable = false)
    private BigDecimal capitalAportado;

    @Column(name = "capital_total", nullable = false)
    private BigDecimal capitalTotal;

    // Valores derivados: se calculan a partir del capital, no se persisten
    // para evitar quedar desincronizados con capitalAportado / capitalTotal.

    @Transient
    public BigDecimal getPlusvalia() {
        if (capitalAportado == null || capitalTotal == null) {
            return null;
        }
        return capitalTotal.subtract(capitalAportado);
    }

    @Transient
    public BigDecimal getPorcentajePlusvalia() {
        if (capitalAportado == null || capitalTotal == null
                || capitalAportado.signum() == 0) {
            return null;
        }
        return getPlusvalia()
                .divide(capitalAportado, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
