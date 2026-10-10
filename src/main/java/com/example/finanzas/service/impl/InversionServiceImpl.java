package com.example.finanzas.service.impl;

import com.example.finanzas.dto.cuenta.TransaccionDTO;
import com.example.finanzas.dto.inversion.ActualizarInversionDTO;
import com.example.finanzas.dto.inversion.CuentaInversionResponse;
import com.example.finanzas.dto.inversion.DistribucionCategoriaResponse;
import com.example.finanzas.dto.inversion.InversionDTO;
import com.example.finanzas.dto.inversion.RendimientoMesResponse;
import com.example.finanzas.dto.inversion.ResumenInversionResponse;
import com.example.finanzas.dto.inversion.RetiradaDTO;
import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.CuentaEntity;
import com.example.finanzas.model.InversionEntity;
import com.example.finanzas.model.TransaccionEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import com.example.finanzas.repository.CategoriaRepository;
import com.example.finanzas.repository.InversionRepository;
import com.example.finanzas.repository.TransaccionRepository;
import com.example.finanzas.service.CuentaService;
import com.example.finanzas.service.InversionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Posiciones de inversión y cuentas de inversión.
 *
 * <p>Modelo de dinero: una cuenta de inversión es una {@link CuentaEntity} de
 * tipo INVERSION cuyo saldo es el <b>efectivo sin invertir</b>. El dinero entra
 * con transferencias desde cuentas corrientes. Aportar a una posición "desde
 * efectivo" genera un movimiento INVERSION negativo en la cuenta (el efectivo
 * baja y la posición sube); retirar desde una posición genera un movimiento
 * INVERSION positivo (venta: la posición baja y el efectivo sube) seguido de
 * una transferencia a la cuenta destino. Así el patrimonio total no cambia por
 * mover dinero entre efectivo y posiciones.
 */
@Service
@RequiredArgsConstructor
public class InversionServiceImpl implements InversionService {

    private final InversionRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final TransaccionRepository transaccionRepository;
    private final CuentaService cuentaService;

    public InversionEntity getInversionById(Long id, UserEntity user){
        InversionEntity inversion = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inversion no encontrada con id " + id));
        verificarPropiedad(inversion, user);
        return inversion;
    }

    public List<InversionEntity> getAllInversiones(UserEntity user){
        return repository.findByUserId(user.getId());
    }

    @Transactional
    public InversionEntity add(InversionDTO inversionDTO, UserEntity user){
        InversionEntity inversion = new InversionEntity();
        inversion.setUser(user);
        inversion.setCategoria(resolverCategoria(inversionDTO.categoriaId(), user));
        inversion.setNombre(limpiarNombre(inversionDTO.nombre()));
        inversion.setCapitalAportado(inversionDTO.capitalAportado());
        inversion.setCapitalTotal(inversionDTO.capitalTotal());

        CuentaEntity cuenta = inversionDTO.cuentaId() != null
                ? cargarCuentaInversion(inversionDTO.cuentaId(), user)
                : null;
        inversion.setCuenta(cuenta);

        if (cuenta != null && Boolean.TRUE.equals(inversionDTO.desdeEfectivo())) {
            exigirEfectivo(cuenta, inversionDTO.capitalAportado());
            InversionEntity guardada = repository.save(inversion);
            registrarApunte(user, cuenta, guardada, inversionDTO.capitalAportado().negate(),
                    "Aportación a " + guardada.getNombreVisible(), LocalDate.now());
            return guardada;
        }
        return repository.save(inversion);
    }

    @Transactional
    public InversionEntity actualizar(Long id, ActualizarInversionDTO dto, UserEntity user){
        InversionEntity inversion = getInversionById(id, user);

        if (dto.aportacion() == null && dto.valorActual() == null && dto.nombre() == null) {
            throw new IllegalArgumentException("Debes indicar una aportación, un valor actual o un nombre");
        }

        if (dto.nombre() != null) {
            inversion.setNombre(limpiarNombre(dto.nombre()));
        }

        // 1) Nueva aportación: suma al capital aportado y al total.
        if (dto.aportacion() != null && dto.aportacion().signum() > 0) {
            // Si sale del efectivo de la cuenta, se registra el movimiento.
            if (inversion.getCuenta() != null && Boolean.TRUE.equals(dto.desdeEfectivo())) {
                CuentaEntity cuenta = cuentaService.getCuenta(inversion.getCuenta().getId(), user);
                exigirEfectivo(cuenta, dto.aportacion());
                registrarApunte(user, cuenta, inversion, dto.aportacion().negate(),
                        "Aportación a " + inversion.getNombreVisible(), LocalDate.now());
            }
            inversion.setCapitalAportado(inversion.getCapitalAportado().add(dto.aportacion()));
            inversion.setCapitalTotal(inversion.getCapitalTotal().add(dto.aportacion()));
        }

        // 2) Valor actual: fija el capital total al valor observado ahora.
        if (dto.valorActual() != null) {
            inversion.setCapitalTotal(dto.valorActual());
        }

        return repository.save(inversion);
    }

    public InversionEntity asignarCuenta(Long id, Long cuentaId, UserEntity user) {
        InversionEntity inversion = getInversionById(id, user);
        inversion.setCuenta(cuentaId != null ? cargarCuentaInversion(cuentaId, user) : null);
        return repository.save(inversion);
    }

    /**
     * Borra la posición. Los movimientos de efectivo que generó (aportaciones,
     * ventas) se conservan: el dinero se movió de verdad y borrarlos alteraría
     * el efectivo de la cuenta.
     */
    public void remove(Long id, UserEntity user) {
        repository.delete(getInversionById(id, user));
    }

    // ── Cuentas de inversión ────────────────────────────────────────────────

    public List<CuentaInversionResponse> getCuentasInversion(UserEntity user) {
        List<CuentaEntity> cuentas = cuentaService.getAllCuentas(user).stream()
                .filter(CuentaEntity::esDeInversion)
                .toList();
        Map<Long, List<InversionEntity>> porCuenta = getAllInversiones(user).stream()
                .filter(i -> i.getCuenta() != null)
                .collect(Collectors.groupingBy(i -> i.getCuenta().getId()));
        Map<Long, BigDecimal> rendimientos = rendimientos12mPorCuenta(user);

        return cuentas.stream()
                .map(c -> resumenCuenta(c, porCuenta.getOrDefault(c.getId(), List.of()),
                        rendimientos.getOrDefault(c.getId(), BigDecimal.ZERO)))
                .sorted(Comparator.comparing(CuentaInversionResponse::valorTotal).reversed())
                .toList();
    }

    public CuentaInversionResponse getCuentaInversion(Long cuentaId, UserEntity user) {
        CuentaEntity cuenta = cargarCuentaInversion(cuentaId, user);
        return resumenCuenta(cuenta, repository.findByCuentaId(cuentaId),
                rendimientos12mPorCuenta(user).getOrDefault(cuentaId, BigDecimal.ZERO));
    }

    public List<RendimientoMesResponse> getRendimientosMensuales(Long cuentaId, UserEntity user) {
        cargarCuentaInversion(cuentaId, user);
        LocalDate desde = inicioVentana12m();
        Map<String, BigDecimal> porMes = new HashMap<>();
        for (Object[] fila : transaccionRepository.ingresosMensualesDeCuenta(cuentaId, desde)) {
            porMes.put(fila[0] + "-" + fila[1], new BigDecimal(fila[2].toString()));
        }
        List<RendimientoMesResponse> serie = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDate mes = desde.plusMonths(i);
            serie.add(new RendimientoMesResponse(mes.getYear(), mes.getMonthValue(),
                    porMes.getOrDefault(mes.getYear() + "-" + mes.getMonthValue(), BigDecimal.ZERO)));
        }
        return serie;
    }

    /**
     * Retirada = (opcionalmente) venta de parte de una posición + transferencia
     * del importe a la cuenta destino. Todo o nada.
     */
    @Transactional
    public void retirar(Long cuentaId, RetiradaDTO dto, UserEntity user) {
        CuentaEntity cuenta = cargarCuentaInversion(cuentaId, user);
        if (dto.cuentaDestinoId().equals(cuentaId)) {
            throw new IllegalArgumentException("La cuenta de origen y la de destino no pueden ser la misma");
        }
        BigDecimal importe = dto.importe();

        if (dto.inversionId() != null) {
            InversionEntity inversion = getInversionById(dto.inversionId(), user);
            if (inversion.getCuenta() == null || !inversion.getCuenta().getId().equals(cuentaId)) {
                throw new IllegalArgumentException("La posición no pertenece a esta cuenta");
            }
            if (importe.compareTo(inversion.getCapitalTotal()) > 0) {
                throw new IllegalArgumentException("No puedes retirar más que el valor actual de la posición ("
                        + inversion.getCapitalTotal().setScale(2, RoundingMode.HALF_UP) + ")");
            }
            // El aportado baja en la misma proporción que el valor: así la
            // rentabilidad (%) de lo que queda no cambia por vender una parte.
            BigDecimal proporcion = importe.divide(inversion.getCapitalTotal(), 10, RoundingMode.HALF_UP);
            BigDecimal aportadoVendido = inversion.getCapitalAportado().multiply(proporcion)
                    .setScale(2, RoundingMode.HALF_UP);
            inversion.setCapitalAportado(inversion.getCapitalAportado().subtract(aportadoVendido));
            inversion.setCapitalTotal(inversion.getCapitalTotal().subtract(importe));
            repository.save(inversion);
            registrarApunte(user, cuenta, inversion, importe,
                    "Venta de " + inversion.getNombreVisible(), dto.fecha());
        } else {
            exigirEfectivo(cuenta, importe);
        }

        String descripcion = dto.descripcion() != null && !dto.descripcion().isBlank()
                ? dto.descripcion()
                : "Retirada de " + cuenta.getNombreCuenta();
        cuentaService.addTransaccionToCuenta(cuentaId, new TransaccionDTO(
                TipoMovimientoEnum.TRANSFERENCIA, null, dto.cuentaDestinoId(),
                importe, descripcion, dto.fecha()), user);
    }

    // ── Totales ─────────────────────────────────────────────────────────────

    public BigDecimal getImporteTotal(UserEntity user) {
        return getAllInversiones(user).stream()
                .map(InversionEntity::getCapitalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCapitalAportadoTotal(UserEntity user) {
        return getAllInversiones(user).stream()
                .map(InversionEntity::getCapitalAportado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPlusvaliaTotal(UserEntity user) {
        return getAllInversiones(user).stream()
                .map(InversionEntity::getPlusvalia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPorcentajeTotal(UserEntity user) {
        BigDecimal aportado = getCapitalAportadoTotal(user);
        if (aportado.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return getPlusvaliaTotal(user)
                .divide(aportado, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public ResumenInversionResponse getResumen(UserEntity user) {
        BigDecimal importeTotal = getImporteTotal(user);
        BigDecimal efectivo = cuentaService.getAllCuentas(user).stream()
                .filter(CuentaEntity::esDeInversion)
                .map(CuentaEntity::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rendimientos = rendimientos12mPorCuenta(user).values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenInversionResponse(
                importeTotal,
                getCapitalAportadoTotal(user),
                getPlusvaliaTotal(user),
                getPorcentajeTotal(user),
                efectivo,
                importeTotal.add(efectivo),
                rendimientos
        );
    }

    public List<DistribucionCategoriaResponse> getDistribucionPorCategoria(UserEntity user) {
        List<InversionEntity> inversiones = getAllInversiones(user);
        BigDecimal total = getImporteTotal(user);

        return inversiones.stream()
                .collect(Collectors.groupingBy(inversion -> inversion.getCategoria().getId()))
                .values().stream()
                .map(inversionesCategoria -> {
                    CategoriaEntity categoria = inversionesCategoria.get(0).getCategoria();
                    BigDecimal capitalCategoria = inversionesCategoria.stream()
                            .map(InversionEntity::getCapitalTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal porcentaje = total.signum() == 0
                            ? BigDecimal.ZERO
                            : capitalCategoria.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    return new DistribucionCategoriaResponse(
                            categoria.getId(),
                            categoria.getNombreCategoria(),
                            categoria.getColor(),
                            capitalCategoria,
                            porcentaje
                    );
                })
                .sorted(Comparator.comparing(DistribucionCategoriaResponse::porcentaje).reversed())
                .toList();
    }

    // ── Apoyo ───────────────────────────────────────────────────────────────

    private CuentaInversionResponse resumenCuenta(CuentaEntity cuenta, List<InversionEntity> posiciones,
                                                  BigDecimal rendimientos12m) {
        BigDecimal aportado = posiciones.stream().map(InversionEntity::getCapitalAportado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valor = posiciones.stream().map(InversionEntity::getCapitalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal plusvalia = valor.subtract(aportado);
        BigDecimal pct = aportado.signum() == 0 ? BigDecimal.ZERO
                : plusvalia.divide(aportado, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        BigDecimal efectivo = cuenta.getSaldo();
        CuentaEntity vinculada = cuenta.getCuentaVinculada();
        return new CuentaInversionResponse(
                cuenta.getId(),
                cuenta.getNombreCuenta(),
                cuenta.getPlataforma(),
                cuenta.getColor(),
                vinculada != null ? vinculada.getId() : null,
                vinculada != null ? vinculada.getNombreCuenta() : null,
                cuenta.getSaldoInicial(),
                efectivo,
                aportado,
                valor,
                efectivo.add(valor),
                plusvalia,
                pct,
                rendimientos12m,
                posiciones.size()
        );
    }

    /** Primer día del mes de hace 11 meses: ventana de 12 meses con el actual incluido. */
    private static LocalDate inicioVentana12m() {
        return LocalDate.now().withDayOfMonth(1).minusMonths(11);
    }

    private Map<Long, BigDecimal> rendimientos12mPorCuenta(UserEntity user) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        for (Object[] fila : transaccionRepository.ingresosCuentasInversionDesde(user.getId(), inicioVentana12m())) {
            mapa.put(((Number) fila[0]).longValue(), new BigDecimal(fila[1].toString()));
        }
        return mapa;
    }

    /** Carga la cuenta (con saldo calculado), comprueba propiedad y que sea de inversión. */
    private CuentaEntity cargarCuentaInversion(Long cuentaId, UserEntity user) {
        CuentaEntity cuenta = cuentaService.getCuenta(cuentaId, user);
        if (!cuenta.esDeInversion()) {
            throw new IllegalArgumentException("La cuenta \"" + cuenta.getNombreCuenta() + "\" no es de inversión");
        }
        return cuenta;
    }

    private static void exigirEfectivo(CuentaEntity cuenta, BigDecimal importe) {
        if (cuenta.getSaldo().compareTo(importe) < 0) {
            throw new IllegalArgumentException("No hay suficiente efectivo en " + cuenta.getNombreCuenta()
                    + " (disponible: " + cuenta.getSaldo().setScale(2, RoundingMode.HALF_UP) + ")");
        }
    }

    /** Movimiento de efectivo ligado a una posición (tipo INVERSION, categoría de la posición). */
    private void registrarApunte(UserEntity user, CuentaEntity cuenta, InversionEntity inversion,
                                 BigDecimal importeConSigno, String descripcion, LocalDate fecha) {
        TransaccionEntity apunte = new TransaccionEntity();
        apunte.setUser(user);
        apunte.setCuenta(cuenta);
        apunte.setTipoMovimiento(TipoMovimientoEnum.INVERSION);
        apunte.setCategoria(inversion.getCategoria());
        apunte.setImporte(importeConSigno);
        apunte.setDescripcion(descripcion);
        apunte.setFechaTransaccion(fecha);
        transaccionRepository.save(apunte);
    }

    private static String limpiarNombre(String nombre) {
        return nombre == null || nombre.isBlank() ? null : nombre.trim();
    }

    private CategoriaEntity resolverCategoria(Long categoriaId, UserEntity user) {
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada con id " + categoriaId));
        if (!categoria.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("La categoria no pertenece al usuario");
        }
        return categoria;
    }

    private void verificarPropiedad(InversionEntity inversion, UserEntity user) {
        if (!inversion.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No tienes acceso a esta inversion");
        }
    }
}
