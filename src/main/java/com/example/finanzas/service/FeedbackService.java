package com.example.finanzas.service;

import com.example.finanzas.dto.feedback.FeedbackDTO;
import com.example.finanzas.model.FeedbackEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.FeedbackEstadoEnum;

import java.util.List;

public interface FeedbackService {
    FeedbackEntity crear(FeedbackDTO dto, UserEntity user);

    // Gestión (admin)
    List<FeedbackEntity> listarTodos();

    FeedbackEntity cambiarEstado(Long id, FeedbackEstadoEnum estado);
}
