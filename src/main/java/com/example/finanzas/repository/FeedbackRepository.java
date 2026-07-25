package com.example.finanzas.repository;

import com.example.finanzas.model.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    List<FeedbackEntity> findByUserIdOrderByFechaCreacionDesc(UUID userId);

    // Para la gestión de incidencias (admin): todo el feedback, más reciente primero.
    List<FeedbackEntity> findAllByOrderByFechaCreacionDesc();
}
