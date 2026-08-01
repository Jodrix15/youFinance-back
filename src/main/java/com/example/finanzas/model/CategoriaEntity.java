package com.example.finanzas.model;

import com.example.finanzas.model.enums.OrigenIngresoEnum;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name= "nombre", nullable = false)
    private String nombreCategoria;

    @Column(name = "tipo_movimiento", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimientoEnum tipo;

    /**
     * Familia del ingreso (Activo / Pasivo / Inversión). Solo tiene valor cuando
     * {@code tipo == INGRESO}; en gastos e inversiones es {@code null}.
     */
    @Column(name = "origen_ingreso")
    @Enumerated(EnumType.STRING)
    private OrigenIngresoEnum origenIngreso;

    /**
     * Color con el que se pinta la categoría en gráficos y listados, en hex
     * {@code #rrggbb}. El usuario no está obligado a elegirlo: si no lo hace,
     * el servicio asigna el primer color libre de la paleta dentro de su tipo.
     */
    @Column(name = "color", length = 7)
    private String color;
}
