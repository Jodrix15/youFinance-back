package com.example.finanzas.service;

import com.example.finanzas.dto.Deuda.DeudaDTO;
import com.example.finanzas.dto.Deuda.ResumenDeudaResponse;
import com.example.finanzas.model.DeudaEntity;
import com.example.finanzas.model.UserEntity;

import java.math.BigDecimal;
import java.util.List;

public interface DeudaService {

    DeudaEntity getDeuda(Long id, UserEntity user);

    List<DeudaEntity> getAllDeudas(UserEntity user);

    DeudaEntity crear(DeudaDTO deudaDTO, UserEntity user);

    DeudaEntity update(Long id, DeudaDTO deudaDTO, UserEntity user);

    BigDecimal getImporteTotal(UserEntity user);

    ResumenDeudaResponse getResumen(UserEntity user);

    void remove(Long id, UserEntity user);
}
