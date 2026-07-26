package com.example.finanzas.model;

import com.example.finanzas.model.enums.FeedbackCategoriaEnum;
import com.example.finanzas.model.enums.FeedbackEstadoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Feedback enviado por un usuario (incidencia, mejora, pregunta u otro). */
@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackCategoriaEnum categoria;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackEstadoEnum estado = FeedbackEstadoEnum.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;
}
