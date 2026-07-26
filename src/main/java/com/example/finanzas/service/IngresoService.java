package com.example.finanzas.service;

import com.example.finanzas.dto.ingreso.EvolucionIngresoResponse;
import com.example.finanzas.dto.ingreso.IngresoCategoriaResponse;
import com.example.finanzas.dto.ingreso.ResumenIngresosResponse;
import com.example.finanzas.model.UserEntity;

import java.util.List;

public interface IngresoService {

    /** Total de ingresos del periodo y su reparto por familia (Activo/Pasivo/Inversión). */
    ResumenIngresosResponse getResumen(UserEntity user, Integer anio, Integer mes);

    /** Total de ingresos por categoría del periodo, de mayor a menor. */
    List<IngresoCategoriaResponse> getPorCategoria(UserEntity user, Integer anio, Integer mes);

    /**
     * Serie mensual continua de ingresos totales (huecos a cero) desde el primer
     * mes con ingresos hasta el mes actual, para la curva de evolución.
     */
    List<EvolucionIngresoResponse> getEvolucion(UserEntity user);
}
