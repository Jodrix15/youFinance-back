package com.example.finanzas.dto.feedback;

import com.example.finanzas.model.enums.FeedbackEstadoEnum;
import jakarta.validation.constraints.NotNull;

/** Cuerpo para cambiar el estado de gestión de un feedback (solo admin). */
public record ActualizarEstadoFeedbackDTO(
        @NotNull FeedbackEstadoEnum estado
) {
}
