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

    /** Nula en las transferencias: un traspaso entre cuentas propias no se categoriza. */
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;

    @Column(nullable = false)
    private BigDecimal importe;

    private String descripcion;

    private LocalDate fechaTransaccion;

    @ManyToOne
    @JoinColumn(name = "cuenta_id", nullable = false)
    private CuentaEntity cuenta;

    /**
     * Identificador compartido por los dos apuntes de una misma transferencia.
     * Null en gastos, ingresos e inversiones.
     */
    @Column(name = "transferencia_id", length = 36)
    private String transferenciaId;

    /**
     * La otra cuenta implicada en la transferencia (destino en el apunte de
     * salida, origen en el de entrada). Se guarda desnormalizada para poder
     * pintar "Transferencia a Ahorro" sin un self-join.
     */
    @ManyToOne
    @JoinColumn(name = "cuenta_contrapartida_id")
    private CuentaEntity cuentaContrapartida;

    // El importe ya se guarda con signo (gasto/inversión en negativo), así que
    // aquí solo se devuelve tal cual. Se mantiene el método por compatibilidad
    // con el cálculo de saldos.
    @Transient
    public BigDecimal getImporteConSigno() {
        return importe;
    }

    @Transient
    public boolean esTransferencia() {
        return tipoMovimiento == TipoMovimientoEnum.TRANSFERENCIA;
    }
}
