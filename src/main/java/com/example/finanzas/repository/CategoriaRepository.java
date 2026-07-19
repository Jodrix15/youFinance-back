package com.example.finanzas.repository;

import com.example.finanzas.model.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
    List<CategoriaEntity> findByUserId(UUID userId);
    Optional<CategoriaEntity> findByIdAndUserId(Long id, UUID userId);
}