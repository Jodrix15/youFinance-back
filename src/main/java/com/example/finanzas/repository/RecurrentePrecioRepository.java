package com.example.finanzas.repository;

import com.example.finanzas.model.Gastos.RecurrentePrecioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecurrentePrecioRepository extends JpaRepository<RecurrentePrecioEntity, Long> {
    List<RecurrentePrecioEntity> findByGastoRecurrenteId(Long gastoRecurrenteId);

    Optional<RecurrentePrecioEntity> findFirstByGastoRecurrenteIdOrderByIdDesc(Long gastoRecurrenteId);
}