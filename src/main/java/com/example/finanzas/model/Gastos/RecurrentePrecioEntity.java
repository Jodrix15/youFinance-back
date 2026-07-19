package com.example.finanzas.model.Gastos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurrente_precio")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecurrentePrecioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gasto_recurrente_id", nullable = false)
    @JsonIgnore
    private GastoRecurrenteEntity gastoRecurrente;

    @Column(nullable = false)
    private LocalDate fechaVariacionImporte;

    @Column(nullable = false)
    private BigDecimal importe;
}
