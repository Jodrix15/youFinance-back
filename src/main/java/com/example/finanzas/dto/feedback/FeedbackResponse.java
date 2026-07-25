package com.example.finanzas.dto.feedback;

import com.example.finanzas.model.FeedbackEntity;
import com.example.finanzas.model.enums.FeedbackCategoriaEnum;
import com.example.finanzas.model.enums.FeedbackEstadoEnum;

import java.time.Instant;

public record FeedbackResponse(
        Long id,
        String usuario,
        FeedbackCategoriaEnum categoria,
        String mensaje,
        FeedbackEstadoEnum estado,
        Instant fechaCreacion
) {
    public static FeedbackResponse from(FeedbackEntity feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getUser() != null ? feedback.getUser().getUsername() : null,
                feedback.getCategoria(),
                feedback.getMensaje(),
                feedback.getEstado(),
                feedback.getFechaCreacion()
        );
    }
}
