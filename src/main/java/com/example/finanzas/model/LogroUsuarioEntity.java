package com.example.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Logro desbloqueado por un usuario (una fila por usuario y código de logro). */
@Entity
@Table(
        name = "logro_usuario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_logro_usuario",
                columnNames = {"user_id", "codigo"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogroUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 60)
    private String codigo;

    @Column(name = "fecha_desbloqueo", nullable = false)
    private Instant fechaDesbloqueo;
}
