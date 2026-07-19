package com.example.finanzas.service;

import com.example.finanzas.dto.inversion.ActualizarInversionDTO;
import com.example.finanzas.dto.inversion.DistribucionCategoriaResponse;
import com.example.finanzas.dto.inversion.InversionDTO;
import com.example.finanzas.dto.inversion.ResumenInversionResponse;
import com.example.finanzas.model.InversionEntity;
import com.example.finanzas.model.UserEntity;

import java.math.BigDecimal;
import java.util.List;

public interface InversionService {

    InversionEntity getInversionById(Long id, UserEntity user);

    List<InversionEntity> getAllInversiones(UserEntity user);

    BigDecimal getImporteTotal(UserEntity user);

    BigDecimal getCapitalAportadoTotal(UserEntity user);

    BigDecimal getPlusvaliaTotal(UserEntity user);

    BigDecimal getPorcentajeTotal(UserEntity user);

    ResumenInversionResponse getResumen(UserEntity user);

    List<DistribucionCategoriaResponse> getDistribucionPorCategoria(UserEntity user);

    InversionEntity add(InversionDTO inversionDTO, UserEntity user);

    InversionEntity actualizar(Long id, ActualizarInversionDTO actualizarInversionDTO, UserEntity user);

    void remove(Long id, UserEntity user);
}
