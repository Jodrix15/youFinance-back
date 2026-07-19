package com.example.finanzas.model;

import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private TipoMovimientoEnum tipoMovimiento;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Column(nullable = false)
    private BigDecimal importe;

    private String descripcion;

    private LocalDate fechaTransaccion;

    @ManyToOne
    @JoinColumn(name = "cuenta_id", nullable = false)
    private CuentaEntity cuenta;

    // El importe ya se guarda con signo (gasto/inversión en negativo), así que
    // aquí solo se devuelve tal cual. Se mantiene el método por compatibilidad
    // con el cálculo de saldos.
    @Transient
    public BigDecimal getImporteConSigno() {
        return importe;
    }
}
