package com.example.finanzas.service.impl;
import com.example.finanzas.service.GastoRecurrenteService;

import com.example.finanzas.dto.gasto.ActualizarGasto;
import com.example.finanzas.dto.gasto.CrearGasto;
import com.example.finanzas.dto.gasto.NuevoPrecioRequest;
import com.example.finanzas.dto.gasto.ResumenRecurrenteResponse;
import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.FrecuenciaEnum;
import com.example.finanzas.model.enums.TipoImporteEnum;
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
        // gastoAnual = suma de los 12 meses del año en curso, cada uno con su importe
        // (ver importeAnual).
        YearMonth mesActual = YearMonth.now();
        BigDecimal gastoMensualFijo = BigDecimal.ZERO;
        BigDecimal gastoMensualVariable = BigDecimal.ZERO;
        BigDecimal gastoAnual = BigDecimal.ZERO;
        for (GastoRecurrenteEntity gasto : items) {
            if (!gasto.isActive()) {
                continue;
            }
            gastoAnual = gastoAnual.add(importeAnual(gasto, mesActual.getYear()));

            BigDecimal importe = gasto.getImporteEnMes(mesActual);
            if (importe == null || importe.signum() == 0) {
                continue;
            }
            BigDecimal cargo = cargoEnMes(gasto, importe, mesActual);
            if (gasto.getTipoImporte() == TipoImporteEnum.VARIABLE) {
                gastoMensualVariable = gastoMensualVariable.add(cargo);
            } else {
                gastoMensualFijo = gastoMensualFijo.add(cargo);
            }
        }

        return new ResumenRecurrenteResponse(
                gastoMensualFijo.add(gastoMensualVariable),
                gastoMensualFijo,
                gastoMensualVariable,
                gastoAnual,
                activos,
                items.size());
    }

    /**
     * Lo que el gasto carga en los 12 meses del año dado, mes a mes:
     * - Fijos: el precio en vigor el día de cobro de cada mes (un cambio hecho
     *   después del cobro empieza a contar el mes siguiente).
     * - Variables: solo el importe apuntado para ESE mes; un mes sin importe
     *   cuenta 0 (no se arrastra el del mes anterior).
     * En ambos casos se respeta cargoEnMes: nada antes del primer pago y los
     * anuales solo en su mes de cargo.
     */
    private static BigDecimal importeAnual(GastoRecurrenteEntity gasto, int anio) {
        BigDecimal total = BigDecimal.ZERO;
        for (int m = 1; m <= 12; m++) {
            YearMonth mes = YearMonth.of(anio, m);
            BigDecimal importe = gasto.getImporteEnMes(mes);
            if (importe == null || importe.signum() == 0) {
                continue;
            }
            total = total.add(cargoEnMes(gasto, importe, mes));
        }
        return total;
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
        gastoRecurrente.setTipoImporte(gastoRecurrenteDTO.tipoImporte() != null
                ? gastoRecurrenteDTO.tipoImporte()
                : TipoImporteEnum.FIJO);
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
        if (datosActualizados.tipoImporte() != null) {
            existente.setTipoImporte(datosActualizados.tipoImporte());
        }
        // Corrección del precio del alta: se sobrescribe el primer precio del
        // historial en lugar de registrar un cambio (no hubo tal cambio, fue un error).
        if (datosActualizados.importeInicial() != null) {
            existente.getHistorial().stream()
                    .min(Comparator.comparing(RecurrentePrecioEntity::getFechaVariacionImporte,
                                    Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(RecurrentePrecioEntity::getId))
                    .ifPresentOrElse(
                            p -> p.setImporte(datosActualizados.importeInicial()),
                            () -> {
                                RecurrentePrecioEntity inicial = new RecurrentePrecioEntity();
                                inicial.setGastoRecurrente(existente);
                                inicial.setFechaVariacionImporte(existente.getFechaPrimerPago() != null
                                        ? existente.getFechaPrimerPago()
                                        : LocalDate.now());
                                inicial.setImporte(datosActualizados.importeInicial());
                                existente.getHistorial().add(inicial);
                            });
        }
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

    /**
     * Registra un importe.
     * - Variables: como mucho uno por mes. Si ese mes ya tenía importe, se
     *   sustituye (corregir el recibo de marzo no duplica marzo).
     * - Fijos: es un cambio de precio con su fecha. Solo se sustituye si ya hay
     *   un cambio en ese mismo día (para corregir un error); si no, se añade,
     *   porque dos cambios en el mismo mes son legítimos y el cálculo mira el
     *   día de cobro.
     */
    @Transactional
    public RecurrentePrecioEntity registrarNuevoPrecio(Long id, NuevoPrecioRequest nuevoImporte, UserEntity user) {
        GastoRecurrenteEntity gastoRecurrente = getGastoRecurrente(id, user);
        LocalDate fecha = nuevoImporte.getFechaVariacionImporte();
        YearMonth mes = YearMonth.from(fecha);

        boolean variable = gastoRecurrente.getTipoImporte() == TipoImporteEnum.VARIABLE;
        RecurrentePrecioEntity precio = gastoRecurrente.getHistorial().stream()
                .filter(p -> p.getFechaVariacionImporte() != null
                        && (variable
                            ? YearMonth.from(p.getFechaVariacionImporte()).equals(mes)
                            : p.getFechaVariacionImporte().equals(fecha)))
                .max(Comparator.comparing(RecurrentePrecioEntity::getId))
                .orElseGet(() -> {
                    RecurrentePrecioEntity nuevo = new RecurrentePrecioEntity();
                    nuevo.setGastoRecurrente(gastoRecurrente);
                    return nuevo;
                });
        precio.setFechaVariacionImporte(fecha);
        precio.setImporte(nuevoImporte.getImporte());
        return precioRepository.save(precio);
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
