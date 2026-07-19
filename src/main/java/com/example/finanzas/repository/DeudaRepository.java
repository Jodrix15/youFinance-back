package com.example.finanzas.repository;

import com.example.finanzas.model.CuentaEntity;
import com.example.finanzas.model.DeudaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeudaRepository extends JpaRepository<DeudaEntity, Long> {
    List<DeudaEntity> findByUserId(UUID userId);
}