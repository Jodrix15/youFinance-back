package com.example.finanzas.model.Gastos;

import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.model.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gasto_recurrente")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GastoRecurrenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Enumerated(EnumType.STRING)
    private TipoPagoEnum tipoPago;

    @Enumerated(EnumType.STRING)
    private FrecuenciaEnum frecuencia;

    private LocalDate fechaPrimerPago;

    // Último pago efectivamente registrado. Null mientras no se haya pagado ninguno.
    private LocalDate fechaUltimoPago;

    @OneToMany(mappedBy = "gastoRecurrente", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<RecurrentePrecioEntity> historial = new ArrayList<>();

    @Column(nullable = false)
    private boolean isActive;

    // Derivado: el próximo pago es el último pago + un periodo; si aún no se ha
    // pagado ninguno, es el primer pago. No se persiste.
    @Transient
    public LocalDate getFechaProximoPago() {
        if (frecuencia == null) {
            return null;
        }
        if (fechaUltimoPago == null) {
            return fechaPrimerPago;
        }
        return switch (frecuencia) {
            case MENSUAL -> fechaUltimoPago.plusMonths(1);
            case ANUAL -> fechaUltimoPago.plusYears(1);
        };
    }
}
