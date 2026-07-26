package com.example.finanzas.service.impl;

import com.example.finanzas.dto.feedback.FeedbackDTO;
import com.example.finanzas.model.FeedbackEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.FeedbackEstadoEnum;
import com.example.finanzas.repository.FeedbackRepository;
import com.example.finanzas.service.FeedbackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository repository;

    @Override
    public FeedbackEntity crear(FeedbackDTO dto, UserEntity user) {
        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setUser(user);
        feedback.setCategoria(dto.categoria());
        feedback.setMensaje(dto.mensaje());
        feedback.setEstado(FeedbackEstadoEnum.PENDIENTE);
        feedback.setFechaCreacion(Instant.now());
        return repository.save(feedback);
    }

    @Override
    public List<FeedbackEntity> listarTodos() {
        return repository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public FeedbackEntity cambiarEstado(Long id, FeedbackEstadoEnum estado) {
        FeedbackEntity feedback = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feedback no encontrado con id " + id));
        feedback.setEstado(estado);
        return repository.save(feedback);
    }
}
