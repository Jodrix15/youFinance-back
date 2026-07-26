package com.example.finanzas.service.impl;

import com.example.finanzas.dto.ingreso.EvolucionIngresoResponse;
import com.example.finanzas.dto.ingreso.IngresoCategoriaResponse;
import com.example.finanzas.dto.ingreso.IngresoFamiliaResponse;
import com.example.finanzas.dto.ingreso.ResumenIngresosResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.OrigenIngresoEnum;
import com.example.finanzas.repository.TransaccionRepository;
import com.example.finanzas.service.IngresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngresoServiceImpl implements IngresoService {

    private final TransaccionRepository transaccionRepository;

    @Override
    public ResumenIngresosResponse getResumen(UserEntity user, Integer anio, Integer mes) {
        List<Object[]> filas = transaccionRepository.ingresosPorFamilia(user.getId(), anio, mes);

        BigDecimal total = BigDecimal.ZERO;
        for (Object[] fila : filas) {
            total = total.add(new BigDecimal(fila[1].toString()));
        }

        List<IngresoFamiliaResponse> familias = new ArrayList<>();
        for (Object[] fila : filas) {
            OrigenIngresoEnum familia = (OrigenIngresoEnum) fila[0];
            BigDecimal suma = new BigDecimal(fila[1].toString());
            familias.add(new IngresoFamiliaResponse(familia, suma, porcentaje(suma, total)));
        }

        return new ResumenIngresosResponse(total, familias);
    }

    @Override
    public List<IngresoCategoriaResponse> getPorCategoria(UserEntity user, Integer anio, Integer mes) {
        return transaccionRepository.ingresosPorCategoria(user.getId(), anio, mes).stream()
                .map(fila -> new IngresoCategoriaResponse(
                        (String) fila[0],
                        (OrigenIngresoEnum) fila[1],
                        new BigDecimal(fila[2].toString())))
                .toList();
    }

    @Override
    public List<EvolucionIngresoResponse> getEvolucion(UserEntity user) {
        List<Object[]> filas = transaccionRepository.ingresosPorMes(user.getId());
        if (filas.isEmpty()) {
            return List.of();
        }

        // Total indexado por mes; los meses sin ingresos se rellenan a cero.
        Map<YearMonth, BigDecimal> porMes = new HashMap<>();
        for (Object[] fila : filas) {
            int anio = ((Number) fila[0]).intValue();
            int mes = ((Number) fila[1]).intValue();
            porMes.put(YearMonth.of(anio, mes), new BigDecimal(fila[2].toString()));
        }

        Object[] primera = filas.get(0);
        YearMonth inicio = YearMonth.of(((Number) primera[0]).intValue(), ((Number) primera[1]).intValue());
        YearMonth fin = YearMonth.now();
        // Si hubiera ingresos con fecha futura, no cortamos la serie antes de ellos.
        Object[] ultima = filas.get(filas.size() - 1);
        YearMonth ultimoDato = YearMonth.of(((Number) ultima[0]).intValue(), ((Number) ultima[1]).intValue());
        if (ultimoDato.isAfter(fin)) {
            fin = ultimoDato;
        }

        List<EvolucionIngresoResponse> serie = new ArrayList<>();
        for (YearMonth ym = inicio; !ym.isAfter(fin); ym = ym.plusMonths(1)) {
            BigDecimal total = porMes.getOrDefault(ym, BigDecimal.ZERO);
            serie.add(new EvolucionIngresoResponse(ym.atDay(1).toString(), total));
        }
        return serie;
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }
}
