package com.example.finanzas.service;

import com.example.finanzas.model.PatrimonioSnapshotEntity;
import com.example.finanzas.model.UserEntity;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardService {

    BigDecimal getPatrimonioNeto(UserEntity user);

    /**
     * Recalcula el patrimonio del usuario y lo guarda como foto del mes actual
     * (crea el registro del mes o lo actualiza si ya existe). Idempotente.
     */
    void capturarSnapshot(UserEntity user);

    /** Histórico de fotos mensuales del patrimonio, de más antigua a más reciente. */
    List<PatrimonioSnapshotEntity> getHistorico(UserEntity user);
}
