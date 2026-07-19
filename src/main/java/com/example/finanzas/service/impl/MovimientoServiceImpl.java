package com.example.finanzas.service.impl;

import com.example.finanzas.dto.movimiento.MovimientoResponse;
import com.example.finanzas.dto.movimiento.MovimientosPageResponse;
import com.example.finanzas.model.TransaccionEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import com.example.finanzas.repository.TransaccionRepository;
import com.example.finanzas.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final TransaccionRepository repository;

    @Override
    public MovimientosPageResponse buscar(UserEntity user,
                                          TipoMovimientoEnum tipo,
                                          Long cuentaId,
                                          Integer anio,
                                          Integer mes,
                                          String q,
                                          Pageable pageable) {
        String like = (q == null || q.isBlank()) ? null : "%" + q.toLowerCase() + "%";

        Page<TransaccionEntity> page = repository.buscar(user.getId(), tipo, cuentaId, anio, mes, like, pageable);
        List<MovimientoResponse> contenido = page.getContent().stream()
                .map(MovimientoResponse::from)
                .toList();

        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;
        BigDecimal inversiones = BigDecimal.ZERO;
        for (Object[] fila : repository.resumenPorTipo(user.getId(), tipo, cuentaId, anio, mes, like)) {
            TipoMovimientoEnum t = (TipoMovimientoEnum) fila[0];
            BigDecimal total = new BigDecimal(fila[1].toString());
            // El importe se guarda con signo; para los KPIs mostramos magnitudes.
            switch (t) {
                case INGRESO -> ingresos = total;
                case GASTO -> gastos = total.abs();
                case INVERSION -> inversiones = total.abs();
            }
        }

        return new MovimientosPageResponse(
                contenido,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                ingresos,
                gastos,
                inversiones,
                ingresos.subtract(gastos)
        );
    }

    @Override
    public List<MovimientoResponse> getTodas(UserEntity user) {
        return repository.findAllByUserIdFetch(user.getId()).stream()
                .map(MovimientoResponse::from)
                .toList();
    }
}
