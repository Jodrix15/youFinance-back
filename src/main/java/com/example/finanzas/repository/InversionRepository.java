package com.example.finanzas.repository;

import com.example.finanzas.model.InversionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InversionRepository extends JpaRepository<InversionEntity, Long> {
    List<InversionEntity> findByUserId(UUID userId);
    boolean existsByCategoriaId(Long categoriaId);
}