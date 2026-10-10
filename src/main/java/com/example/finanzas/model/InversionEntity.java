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

    /** Activo concreto (p. ej. "MSCI World"). Opcional: sin él se muestra la categoría. */
    @Column(name = "nombre", length = 120)
    private String nombre;

    /** Nombre para mostrar: el del activo o, si no tiene, el de la categoría. */
    @Transient
    public String getNombreVisible() {
        if (nombre != null && !nombre.isBlank()) return nombre;
        return categoria != null ? categoria.getNombreCategoria() : "#" + id;
    }

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    /**
     * Cuenta de inversión donde está la posición. Null en las inversiones
     * creadas antes de existir las cuentas de inversión ("sin cuenta").
     */
    @ManyToOne
    @JoinColumn(name = "cuenta_id")
    private CuentaEntity cuenta;

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
