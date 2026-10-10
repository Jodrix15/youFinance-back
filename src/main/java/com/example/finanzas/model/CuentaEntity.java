package com.example.finanzas.model;

import jakarta.persistence.*;
import com.example.finanzas.model.enums.PlataformaInversionEnum;
import com.example.finanzas.model.enums.TipoCuentaEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre", nullable=false)
    private String nombreCuenta;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** Corriente (banco) o de inversión (Mintos, broker…). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoCuentaEnum tipo = TipoCuentaEnum.CORRIENTE;

    /** Solo en cuentas de inversión: clase de plataforma (etiqueta). */
    @Enumerated(EnumType.STRING)
    @Column(name = "plataforma")
    private PlataformaInversionEnum plataforma;

    /** Color hex (#rrggbb) para tarjetas y gráficos. Opcional. */
    @Column(name = "color", length = 7)
    private String color;

    /**
     * Solo en cuentas de inversión: cuenta corriente que se propone por
     * defecto al retirar dinero.
     */
    @ManyToOne
    @JoinColumn(name = "cuenta_vinculada_id")
    private CuentaEntity cuentaVinculada;

    /**
     * Saldo de partida de la cuenta, el que teclea el usuario al crearla.
     * Es un dato inmutable del histórico: NO se toca al registrar movimientos.
     */
    @Column(name = "saldo_inicial", nullable = false)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cuenta")
    private List<TransaccionEntity> transacciones = new ArrayList<>();

    /**
     * Saldo actual = saldoInicial + suma de las transacciones de la cuenta.
     * No se persiste: lo rellena el servicio al leer, con una sola consulta
     * agregada. Antes era una columna que se mutaba en cada escritura y bastaba
     * un fallo en cualquiera de ellas para que quedara desincronizada para
     * siempre; derivarlo hace que ese error sea imposible por construcción.
     */
    @Transient
    private BigDecimal saldo;

    public boolean esDeInversion() {
        return tipo == TipoCuentaEnum.INVERSION;
    }

    public BigDecimal getSaldo() {
        return saldo != null ? saldo : saldoInicial;
    }

    /** Rellena el saldo calculado a partir de la suma de movimientos. */
    public void hidratarSaldo(BigDecimal sumaTransacciones) {
        BigDecimal base = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        this.saldo = base.add(sumaTransacciones != null ? sumaTransacciones : BigDecimal.ZERO);
    }
}
