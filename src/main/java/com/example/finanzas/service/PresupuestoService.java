package com.example.finanzas.service;

import com.example.finanzas.dto.presupuesto.PresupuestoDTO;
import com.example.finanzas.model.PresupuestoEntity;
import com.example.finanzas.model.UserEntity;

import java.util.List;

public interface PresupuestoService {

    List<PresupuestoEntity> getAll(UserEntity user);

    PresupuestoEntity get(Long id, UserEntity user);

    PresupuestoEntity crear(PresupuestoDTO dto, UserEntity user);

    PresupuestoEntity update(Long id, PresupuestoDTO dto, UserEntity user);

    void remove(Long id, UserEntity user);
}
