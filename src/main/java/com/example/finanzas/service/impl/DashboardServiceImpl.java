package com.example.finanzas.service.impl;

import com.example.finanzas.dto.dashboard.DistribucionPatrimonioResponse;
import com.example.finanzas.dto.dashboard.FlujoCajaMesResponse;
import com.example.finanzas.dto.dashboard.GastoCategoriaResponse;
import com.example.finanzas.dto.dashboard.GastosFijosMesResponse;
import com.example.finanzas.dto.inversion.DistribucionCategoriaResponse;
import com.example.finanzas.model.DeudaEntity;
import com.example.finanzas.model.Gastos.GastoRecurrenteEntity;
import com.example.finanzas.model.Gastos.RecurrentePrecioEntity;
import com.example.finanzas.model.PatrimonioSnapshotEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.repository.PatrimonioSnapshotRepository;
import com.example.finanzas.repository.TransaccionRepository;
import com.example.finanzas.service.CuentaService;
import com.example.finanzas.service.DashboardService;
import com.example.finanzas.service.DeudaService;
import com.example.finanzas.service.GastoRecurrenteService;
import com.example.finanzas.service.InversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CuentaService cuentaService;
    private final InversionService inversionService;
    private final DeudaService deudaService;
    private final GastoRecurrenteService gastoRecurrenteService;
    private final TransaccionRepository transaccionRepository;
    private final PatrimonioSnapshotRepository snapshotRepository;

    @Override
    public BigDecimal getPatrimonioNeto(UserEntity user) {
        return cuentaService.getImporteTotal(user)
                .add(inversionService.getImporteTotal(user))
                .subtract(deudaService.getImporteTotal(user));
    }

    @Override
    public List<DistribucionPatrimonioResponse> getDistribucionPatrimonio(UserEntity user) {
        BigDecimal totalCuentas = cuentaService.getImporteTotal(user);
        List<DistribucionCategoriaResponse> inversiones = inversionService.getDistribucionPorCategoria(user);

        BigDecimal totalInversiones = inversiones.stream()
                .map(DistribucionCategoriaResponse::capitalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Total de referencia para los porcentajes: todo el patrimonio menos las deudas.
        BigDecimal total = totalCuentas.add(totalInversiones);

        List<DistribucionPatrimonioResponse> distribucion = new ArrayList<>();
        if (totalCuentas.signum() > 0) {
            distribucion.add(new DistribucionPatrimonioResponse(
                    "Cuentas", totalCuentas, porcentaje(totalCuentas, total)));
        }
        for (DistribucionCategoriaResponse inversion : inversiones) {
            distribucion.add(new DistribucionPatrimonioResponse(
                    inversion.categoriaNombre(),
                    inversion.capitalTotal(),
                    porcentaje(inversion.capitalTotal(), total)));
        }
        return distribucion;
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    @Override
    @Transactional
    public void capturarSnapshot(UserEntity user) {
        BigDecimal cuentas = cuentaService.getImporteTotal(user);
        BigDecimal inversiones = inversionService.getImporteTotal(user);
        BigDecimal deudas = deudaService.getImporteTotal(user);
        BigDecimal patrimonio = cuentas.add(inversiones).subtract(deudas);

        LocalDate mes = LocalDate.now().withDayOfMonth(1);

        // Upsert: si ya hay foto de este mes se actualiza; si no, se crea.
        PatrimonioSnapshotEntity snapshot = snapshotRepository
                .findByUserIdAndMes(user.getId(), mes)
                .orElseGet(() -> PatrimonioSnapshotEntity.builder()
                        .user(user)
                        .mes(mes)
                        .build());

        snapshot.setTotalCuentas(cuentas);
        snapshot.setTotalInversiones(inversiones);
        snapshot.setTotalDeudas(deudas);
        snapshot.setPatrimonioNeto(patrimonio);
        snapshot.setActualizadoEn(Instant.now());

        snapshotRepository.save(snapshot);
    }

    @Override
    public List<PatrimonioSnapshotEntity> getHistorico(UserEntity user) {
        return snapshotRepository.findByUserIdOrderByMesAsc(user.getId());
    }

    @Override
    public List<FlujoCajaMesResponse> getFlujoCaja(UserEntity user, int anio) {
        BigDecimal[] ingresos = new BigDecimal[12];
        BigDecimal[] gastos = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            ingresos[i] = BigDecimal.ZERO;
            gastos[i] = BigDecimal.ZERO;
        }
        for (Object[] fila : transaccionRepository.totalesPorMesYTipo(user.getId(), anio)) {
            int mes = ((Number) fila[0]).intValue();
            TipoMovimientoEnum tipo = (TipoMovimientoEnum) fila[1];
            BigDecimal suma = new BigDecimal(fila[2].toString()).abs();
            if (mes < 1 || mes > 12) {
                continue;
            }
            if (tipo == TipoMovimientoEnum.INGRESO) {
                ingresos[mes - 1] = suma;
            } else if (tipo == TipoMovimientoEnum.GASTO) {
                gastos[mes - 1] = suma;
            }
        }
        List<FlujoCajaMesResponse> flujo = new ArrayList<>(12);
        for (int mes = 1; mes <= 12; mes++) {
            flujo.add(new FlujoCajaMesResponse(mes, ingresos[mes - 1], gastos[mes - 1]));
        }
        return flujo;
    }

    @Override
    public List<GastoCategoriaResponse> getGastosPorCategoria(UserEntity user) {
        return transaccionRepository.gastosPorCategoria(user.getId()).stream()
                .map(fila -> new GastoCategoriaResponse(
                        (String) fila[0],
                        new BigDecimal(fila[1].toString())))
                .toList();
    }

    @Override
    public GastosFijosMesResponse getGastosFijosMes(UserEntity user, int anio, int mes) {
        YearMonth objetivo = YearMonth.of(anio, mes);

        BigDecimal suscripciones = BigDecimal.ZERO;
        BigDecimal recurrentes = BigDecimal.ZERO;
        for (GastoRecurrenteEntity gasto : gastoRecurrenteService.getAllGastosRecurrentes(user)) {
            BigDecimal importe = importeRecurrenteEnMes(gasto, objetivo);
            if (gasto.getTipoPago() == TipoPagoEnum.SUSCRIPCION) {
                suscripciones = suscripciones.add(importe);
            } else {
                recurrentes = recurrentes.add(importe);
            }
        }

        BigDecimal cuotasDeuda = BigDecimal.ZERO;
        for (DeudaEntity deuda : deudaService.getAllDeudas(user)) {
            cuotasDeuda = cuotasDeuda.add(importeDeudaEnMes(deuda, objetivo));
        }

        BigDecimal total = suscripciones.add(recurrentes).add(cuotasDeuda);
        return new GastosFijosMesResponse(suscripciones, recurrentes, cuotasDeuda, total);
    }

    /**
     * Importe que un recurrente/suscripción carga realmente en el mes dado.
     * Sin fecha de fin: activo desde el primer pago hasta que se desactiva.
     * Mensual → cada mes; anual → solo en el mes ancla (el del primer pago).
     */
    private BigDecimal importeRecurrenteEnMes(GastoRecurrenteEntity gasto, YearMonth objetivo) {
        if (!gasto.isActive()) {
            return BigDecimal.ZERO;
        }
        BigDecimal importe = gasto.getHistorial().stream()
                .max(Comparator.comparing(RecurrentePrecioEntity::getId))
                .map(RecurrentePrecioEntity::getImporte)
                .orElse(BigDecimal.ZERO);
        if (importe.signum() == 0) {
            return BigDecimal.ZERO;
        }
        LocalDate primerPago = gasto.getFechaPrimerPago();
        if (primerPago != null && objetivo.isBefore(YearMonth.from(primerPago))) {
            return BigDecimal.ZERO;
        }
        if (gasto.getFrecuencia() == FrecuenciaEnum.MENSUAL) {
            return importe;
        }
        // Anual: cae una vez al año, en el mes del primer pago (o del próximo).
        LocalDate ancla = primerPago != null ? primerPago : gasto.getFechaProximoPago();
        if (ancla == null) {
            return BigDecimal.ZERO;
        }
        return objetivo.getMonthValue() == ancla.getMonthValue() ? importe : BigDecimal.ZERO;
    }

    /**
     * Cuota que una deuda carga realmente en el mes dado. Mensual → cada mes
     * hasta el vencimiento; anual → aproximada al mes del vencimiento (las
     * deudas no tienen fecha de próximo pago).
     */
    private BigDecimal importeDeudaEnMes(DeudaEntity deuda, YearMonth objetivo) {
        BigDecimal cuota = deuda.getCuota() != null ? deuda.getCuota() : BigDecimal.ZERO;
        if (cuota.signum() == 0) {
            return BigDecimal.ZERO;
        }
        LocalDate vencimiento = deuda.getFechaVencimiento();
        if (vencimiento != null && objetivo.isAfter(YearMonth.from(vencimiento))) {
            return BigDecimal.ZERO;
        }
        if (deuda.getFrecuencia() == FrecuenciaEnum.MENSUAL) {
            return cuota;
        }
        if (vencimiento == null) {
            return BigDecimal.ZERO;
        }
        return objetivo.getMonthValue() == vencimiento.getMonthValue() ? cuota : BigDecimal.ZERO;
    }
}
