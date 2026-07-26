package com.example.finanzas.controller;

import com.example.finanzas.dto.feedback.ActualizarEstadoFeedbackDTO;
import com.example.finanzas.dto.feedback.FeedbackDTO;
import com.example.finanzas.dto.feedback.FeedbackResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /** Cualquier usuario autenticado puede enviar feedback. */
    @PostMapping
    public ResponseEntity<FeedbackResponse> enviar(@Valid @RequestBody FeedbackDTO dto,
                                                   @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FeedbackResponse.from(feedbackService.crear(dto, user)));
    }

    /** Listado de todo el feedback (solo admin; protegido en SecurityConfig). */
    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> listar() {
        List<FeedbackResponse> lista = feedbackService.listarTodos().stream()
                .map(FeedbackResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    /** Cambia el estado de gestión de un feedback (solo admin). */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<FeedbackResponse> cambiarEstado(@PathVariable Long id,
                                                          @Valid @RequestBody ActualizarEstadoFeedbackDTO dto) {
        return ResponseEntity.ok(FeedbackResponse.from(feedbackService.cambiarEstado(id, dto.estado())));
    }
}
