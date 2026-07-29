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
import java.util.Collections;
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
        List<Object[]> filas = transaccionRepository.ingresosPorMesYFamilia(user.getId());
        if (filas.isEmpty()) {
            return List.of();
        }

        // Desglose por familia indexado por mes; los meses sin ingresos se
        // rellenan a cero para que la curva no se corte.
        Map<YearMonth, Map<OrigenIngresoEnum, BigDecimal>> porMes = new HashMap<>();
        YearMonth inicio = null;
        YearMonth ultimoDato = null;
        for (Object[] fila : filas) {
            YearMonth ym = YearMonth.of(((Number) fila[0]).intValue(), ((Number) fila[1]).intValue());
            // La familia es null cuando la categoría todavía no está clasificada.
            OrigenIngresoEnum familia = (OrigenIngresoEnum) fila[2];
            BigDecimal suma = new BigDecimal(fila[3].toString());

            porMes.computeIfAbsent(ym, k -> new HashMap<>())
                    .merge(familia, suma, BigDecimal::add);

            if (inicio == null || ym.isBefore(inicio)) {
                inicio = ym;
            }
            if (ultimoDato == null || ym.isAfter(ultimoDato)) {
                ultimoDato = ym;
            }
        }

        YearMonth fin = YearMonth.now();
        // Si hubiera ingresos con fecha futura, no cortamos la serie antes de ellos.
        if (ultimoDato.isAfter(fin)) {
            fin = ultimoDato;
        }

        List<EvolucionIngresoResponse> serie = new ArrayList<>();
        for (YearMonth ym = inicio; !ym.isAfter(fin); ym = ym.plusMonths(1)) {
            // Collections.emptyMap() y no Map.of(): este último no admite
            // consultas con clave null, y null es la familia "sin clasificar".
            Map<OrigenIngresoEnum, BigDecimal> mesActual =
                    porMes.getOrDefault(ym, Collections.emptyMap());
            BigDecimal activo = mesActual.getOrDefault(OrigenIngresoEnum.ACTIVO, BigDecimal.ZERO);
            BigDecimal pasivo = mesActual.getOrDefault(OrigenIngresoEnum.PASIVO, BigDecimal.ZERO);
            BigDecimal inversion = mesActual.getOrDefault(OrigenIngresoEnum.INVERSION, BigDecimal.ZERO);
            // La clave null agrupa las categorías sin familia asignada.
            BigDecimal sinClasificar = mesActual.getOrDefault(null, BigDecimal.ZERO);
            BigDecimal total = activo.add(pasivo).add(inversion).add(sinClasificar);

            serie.add(new EvolucionIngresoResponse(
                    ym.atDay(1).toString(), total, activo, pasivo, inversion, sinClasificar));
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
