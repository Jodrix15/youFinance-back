package com.example.finanzas.service;

import com.example.finanzas.dto.dashboard.DistribucionPatrimonioResponse;
import com.example.finanzas.dto.dashboard.FlujoCajaMesResponse;
import com.example.finanzas.dto.dashboard.GastoCategoriaResponse;
import com.example.finanzas.dto.dashboard.GastosFijosMesResponse;
import com.example.finanzas.model.PatrimonioSnapshotEntity;
import com.example.finanzas.model.UserEntity;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardService {

    BigDecimal getPatrimonioNeto(UserEntity user);

    /**
     * Reparto del patrimonio: cuentas + cada categoría de inversión (sin deudas),
     * con el porcentaje de cada concepto sobre el total.
     */
    List<DistribucionPatrimonioResponse> getDistribucionPatrimonio(UserEntity user);

    /**
     * Recalcula el patrimonio del usuario y lo guarda como foto del mes actual
     * (crea el registro del mes o lo actualiza si ya existe). Idempotente.
     */
    void capturarSnapshot(UserEntity user);

    /** Histórico de fotos mensuales del patrimonio, de más antigua a más reciente. */
    List<PatrimonioSnapshotEntity> getHistorico(UserEntity user);

    /** Ingresos y gastos por mes (1-12) del año indicado. Siempre 12 entradas. */
    List<FlujoCajaMesResponse> getFlujoCaja(UserEntity user, int anio);

    /** Total gastado por categoría, de mayor a menor. */
    List<GastoCategoriaResponse> getGastosPorCategoria(UserEntity user);

    /**
     * Gasto fijo que vence en el mes indicado: suscripciones y recurrentes
     * activos (los anuales solo en su mes) + cuotas de deuda vigentes.
     */
    GastosFijosMesResponse getGastosFijosMes(UserEntity user, int anio, int mes);
}
