package com.example.finanzas.service;

import com.example.finanzas.dto.gasto.ActualizarGasto;
import com.example.finanzas.dto.gasto.CrearGasto;
import com.example.finanzas.dto.gasto.NuevoPrecioRequest;
import com.example.finanzas.dto.gasto.ResumenRecurrenteResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.model.Gastos.GastoRecurrenteEntity;
import com.example.finanzas.model.Gastos.RecurrentePrecioEntity;

import java.util.List;

public interface GastoRecurrenteService {

    GastoRecurrenteEntity getGastoRecurrente(Long id, UserEntity user);

    List<GastoRecurrenteEntity> getAllGastosRecurrentes(UserEntity user);

    ResumenRecurrenteResponse getResumen(UserEntity user, TipoPagoEnum tipoPago);

    RecurrentePrecioEntity getImporteActual(Long id, UserEntity user);

    GastoRecurrenteEntity add(CrearGasto gastoRecurrenteDTO, UserEntity user);

    GastoRecurrenteEntity update(Long id, ActualizarGasto datosActualizados, UserEntity user);

    RecurrentePrecioEntity registrarNuevoPrecio(Long id, NuevoPrecioRequest nuevoImporte, UserEntity user);

    GastoRecurrenteEntity registrarPago(Long id, UserEntity user);

    void remove(Long id, UserEntity user);
}
