package com.example.finanzas.repository;

import com.example.finanzas.model.Gastos.GastoRecurrenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GastoRecurrenteRepository extends JpaRepository<GastoRecurrenteEntity, Long> {
    List<GastoRecurrenteEntity> findByUserId(UUID userId);
    boolean existsByCategoriaId(Long categoriaId);
}