package com.example.finanzas.repository;

import com.example.finanzas.model.LogroUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LogroUsuarioRepository extends JpaRepository<LogroUsuarioEntity, Long> {
    List<LogroUsuarioEntity> findByUserId(UUID userId);
}
