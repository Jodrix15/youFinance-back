package com.example.finanzas.dto.feedback;

import com.example.finanzas.model.enums.FeedbackCategoriaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de la petición para enviar feedback. */
public record FeedbackDTO(
        @NotNull FeedbackCategoriaEnum categoria,
        @NotBlank @Size(max = 2000) String mensaje
) {
}
