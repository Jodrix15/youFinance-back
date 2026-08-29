package com.example.finanzas.service.impl;
import com.example.finanzas.service.GastoRecurrenteService;

import com.example.finanzas.dto.gasto.ActualizarGasto;
import com.example.finanzas.dto.gasto.CrearGasto;
import com.example.finanzas.dto.gasto.NuevoPrecioRequest;
import com.example.finanzas.dto.gasto.ResumenRecurrenteResponse;
import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoPagoEnum;
import com.example.finanzas.model.Gastos.GastoRecurrenteEntity;
import com.example.finanzas.model.Gastos.RecurrentePeriodoEntity;
import com.example.finanzas.model.Gastos.RecurrentePrecioEntity;
import com.example.finanzas.repository.CategoriaRepository;
import com.example.finanzas.repository.GastoRecurrenteRepository;
import com.example.finanzas.repository.RecurrentePrecioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoRecurrenteServiceImpl implements GastoRecurrenteService {

    private final GastoRecurrenteRepository repository;
    private final RecurrentePrecioRepository precioRepository;
    private final CategoriaRepository categoriaRepository;

    public GastoRecurrenteEntity getGastoRecurrente(Long id, UserEntity user) {
        GastoRecurrenteEntity gastoRecurrente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("GastoRecurrente no encontrado con id " + id));
        verificarPropiedad(gastoRecurrente, user);
        return gastoRecurrente;
    }

    public List<GastoRecurrenteEntity> getAllGastosRecurrentes(UserEntity user) {
        return repository.findByUserId(user.getId());
    }

    public ResumenRecurrenteResponse getResumen(UserEntity user, TipoPagoEnum tipoPago) {
        List<GastoRecurrenteEntity> items = getAllGastosRecurrentes(user).stream()
                .filter(g -> g.getTipoPago() == tipoPago)
                .toList();

        long activos = items.stream().filter(GastoRecurrenteEntity::isActive).count();

        // gastoMensual = lo que se cobra REALMENTE en el mes en curso (no un promedio):
        // los mensuales cuentan siempre y los anuales solo en su mes de cargo.
        // gastoAnual = coste real de un año completo a este ritmo.
        YearMonth mesActual = YearMonth.now();
        BigDecimal gastoMensual = BigDecimal.ZERO;
        BigDecimal gastoAnual = BigDecimal.ZERO;
        for (GastoRecurrenteEntity gasto : items) {
            if (!gasto.isActive()) {
                continue;
            }
            BigDecimal importe = importeVigente(gasto);
            if (importe.signum() == 0) {
                continue;
            }
            gastoAnual = gastoAnual.add(gasto.getFrecuencia() == FrecuenciaEnum.ANUAL
                    ? importe
                    : importe.multiply(BigDecimal.valueOf(12)));
            gastoMensual = gastoMensual.add(cargoEnMes(gasto, importe, mesActual));
        }

        return new ResumenRecurrenteResponse(gastoMensual, gastoAnual, activos, items.size());
    }

    /** Importe actual del gasto (último precio del historial). */
    private static BigDecimal importeVigente(GastoRecurrenteEntity gasto) {
        BigDecimal importe = gasto.getHistorial().stream()
                .max(Comparator.comparing(RecurrentePrecioEntity::getId))
                .map(RecurrentePrecioEntity::getImporte)
                .orElse(BigDecimal.ZERO);
        return importe == null ? BigDecimal.ZERO : importe;
    }

    /**
     * Importe que el gasto carga realmente en el mes dado. Mismo criterio que el
     * widget de gastos fijos del dashboard: nada antes del primer pago, los
     * mensuales cada mes y los anuales solo en el mes ancla (el del primer pago,
     * o el del próximo si no hay primero).
     */
    private static BigDecimal cargoEnMes(GastoRecurrenteEntity gasto, BigDecimal importe, YearMonth objetivo) {
        LocalDate primerPago = gasto.getFechaPrimerPago();
        if (primerPago != null && objetivo.isBefore(YearMonth.from(primerPago))) {
            return BigDecimal.ZERO;
        }
        if (gasto.getFrecuencia() == FrecuenciaEnum.MENSUAL) {
            return importe;
        }
        LocalDate ancla = primerPago != null ? primerPago : gasto.getFechaProximoPago();
        if (ancla == null) {
            return BigDecimal.ZERO;
        }
        return objetivo.getMonthValue() == ancla.getMonthValue() ? importe : BigDecimal.ZERO;
    }

    public RecurrentePrecioEntity getImporteActual(Long id, UserEntity user) {
        getGastoRecurrente(id, user); // valida existencia y propiedad
        return precioRepository.findFirstByGastoRecurrenteIdOrderByIdDesc(id)
                .orElseThrow(() -> new EntityNotFoundException("Importe no encontrado con id " + id));
    }

    @Transactional
    public GastoRecurrenteEntity add(CrearGasto gastoRecurrenteDTO, UserEntity user) {
        GastoRecurrenteEntity gastoRecurrente = new GastoRecurrenteEntity();
        gastoRecurrente.setUser(user);
        gastoRecurrente.setCategoria(resolverCategoria(gastoRecurrenteDTO.categoriaId(), user));
        gastoRecurrente.setNombre(gastoRecurrenteDTO.nombre());
        gastoRecurrente.setFrecuencia(gastoRecurrenteDTO.frecuencia());
        gastoRecurrente.setTipoPago(gastoRecurrenteDTO.tipoPago());
        // Nace activo, con su primer periodo abierto desde la fecha de primer pago.
        gastoRecurrente.setActive(true);
        gastoRecurrente.getPeriodos()
                .add(abrirPeriodo(gastoRecurrente, gastoRecurrenteDTO.fechaPrimerPago()));

        GastoRecurrenteEntity guardado = repository.save(gastoRecurrente);

        RecurrentePrecioEntity recurrentePrecio = new RecurrentePrecioEntity();
        recurrentePrecio.setGastoRecurrente(guardado);
        recurrentePrecio.setFechaVariacionImporte(LocalDate.now());
        recurrentePrecio.setImporte(gastoRecurrenteDTO.importeInicial());
        precioRepository.save(recurrentePrecio);

        return guardado;
    }

    @Transactional
    public GastoRecurrenteEntity update(Long id, ActualizarGasto datosActualizados, UserEntity user) {
        GastoRecurrenteEntity existente = getGastoRecurrente(id, user);
        boolean estabaActivo = existente.isActive();
        boolean quedaActivo = datosActualizados.active();

        existente.setNombre(datosActualizados.nombre());
        existente.setCategoria(resolverCategoria(datosActualizados.categoriaId(), user));
        existente.setTipoPago(datosActualizados.tipoPago());
        existente.setFrecuencia(datosActualizados.frecuencia());
        existente.setActive(quedaActivo);

        // El ciclo de vida lo lleva el servidor, no el formulario. Dar de baja
        // cierra el periodo vigente y reactivar abre otro nuevo: el tramo anterior
        // se conserva entero, con sus fechas y sus pagos.
        if (estabaActivo && !quedaActivo) {
            existente.getPeriodoActual().ifPresent(p -> p.setFechaFin(LocalDate.now()));
        } else if (!estabaActivo && quedaActivo) {
            existente.getPeriodos().add(abrirPeriodo(existente, LocalDate.now()));
        } else {
            // Sin cambio de estado el formulario sí puede corregir el alta del tramo.
            existente.getUltimoPeriodo()
                    .ifPresent(p -> p.setFechaInicio(datosActualizados.fechaPrimerPago()));
        }

        return repository.save(existente);
    }

    /** Nuevo tramo de vida abierto en la fecha indicada, aún sin pagos. */
    private static RecurrentePeriodoEntity abrirPeriodo(GastoRecurrenteEntity gasto, LocalDate desde) {
        RecurrentePeriodoEntity periodo = new RecurrentePeriodoEntity();
        periodo.setGastoRecurrente(gasto);
        periodo.setFechaInicio(desde);
        return periodo;
    }

    public RecurrentePrecioEntity registrarNuevoPrecio(Long id, NuevoPrecioRequest nuevoImporte, UserEntity user) {
        GastoRecurrenteEntity gastoRecurrente = getGastoRecurrente(id, user);
        RecurrentePrecioEntity nuevoPrecio = new RecurrentePrecioEntity();
        nuevoPrecio.setGastoRecurrente(gastoRecurrente);
        nuevoPrecio.setFechaVariacionImporte(nuevoImporte.getFechaVariacionImporte());
        nuevoPrecio.setImporte(nuevoImporte.getImporte());
        return precioRepository.save(nuevoPrecio);
    }

    public void remove(Long id, UserEntity user) {
        repository.delete(getGastoRecurrente(id, user));
    }

    private CategoriaEntity resolverCategoria(Long categoriaId, UserEntity user) {
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada con id " + categoriaId));
        if (!categoria.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("La categoria no pertenece al usuario");
        }
        return categoria;
    }

    private void verificarPropiedad(GastoRecurrenteEntity gastoRecurrente, UserEntity user) {
        if (!gastoRecurrente.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No tienes acceso a este gasto recurrente");
        }
    }
}
