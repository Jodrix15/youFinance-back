package com.example.finanzas.service;

import com.example.finanzas.dto.movimiento.MovimientoResponse;
import com.example.finanzas.dto.movimiento.MovimientosPageResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovimientoService {

    MovimientosPageResponse buscar(UserEntity user,
                                   TipoMovimientoEnum tipo,
                                   Long cuentaId,
                                   Integer anio,
                                   Integer mes,
                                   String q,
                                   Pageable pageable);

    /** Todos los movimientos del usuario en una sola consulta. */
    List<MovimientoResponse> getTodas(UserEntity user);
}
