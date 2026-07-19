package com.example.finanzas.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private BigDecimal importe =  BigDecimal.ZERO;

    @OneToMany(mappedBy = "cuenta")
    private List<TransaccionEntity> transacciones = new ArrayList<>();

    public void aplicarTransaccion(TransaccionEntity transaccion){
        this.importe = this.importe.add(transaccion.getImporteConSigno());
    }

}
