package com.example.finanzas.service.impl;
import com.example.finanzas.model.*;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import com.example.finanzas.service.CuentaService;

import com.example.finanzas.dto.cuenta.CuentaDTO;
import com.example.finanzas.dto.cuenta.ResumenCuentaResponse;
import com.example.finanzas.dto.cuenta.TransaccionDTO;
import com.example.finanzas.repository.CategoriaRepository;
import com.example.finanzas.repository.CuentaRepository;
import com.example.finanzas.repository.TransaccionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository repository;
    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;

    // ── Lectura ──────────────────────────────────────────────────────────────

    public CuentaEntity getCuenta(Long id, UserEntity user) {
        CuentaEntity cuenta = cargarCuenta(id, user);
        cuenta.hidratarSaldo(transaccionRepository.sumaDeCuenta(id));
        return cuenta;
    }

    public List<CuentaEntity> getAllCuentas(UserEntity user) {
        List<CuentaEntity> cuentas = repository.findByUserId(user.getId());
        // Una sola consulta agregada para todas las cuentas (evita el N+1).
        Map<Long, BigDecimal> sumas = new HashMap<>();
        for (Object[] fila : transaccionRepository.sumaPorCuenta(user.getId())) {
            sumas.put(((Number) fila[0]).longValue(), new BigDecimal(fila[1].toString()));
        }
        cuentas.forEach(c -> c.hidratarSaldo(sumas.getOrDefault(c.getId(), BigDecimal.ZERO)));
        return cuentas;
    }

    public BigDecimal getImporteTotal(UserEntity user) {
        return getAllCuentas(user).stream()
                .map(CuentaEntity::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ResumenCuentaResponse getResumen(UserEntity user, Integer anio, Integer mes) {
        List<CuentaEntity> cuentas = getAllCuentas(user);
        BigDecimal totalCuentas = cuentas.stream()
                .map(CuentaEntity::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;
        // Reutiliza el agregado por tipo del histórico (sin filtrar por cuenta ni texto).
        // Las transferencias vienen en el resultado pero se ignoran: un traspaso
        // entre cuentas propias no es ni ingreso ni gasto.
        for (Object[] fila : transaccionRepository.resumenPorTipo(user.getId(), null, null, anio, mes, null)) {
            TipoMovimientoEnum tipo = (TipoMovimientoEnum) fila[0];
            BigDecimal suma = new BigDecimal(fila[1].toString());
            if (tipo == TipoMovimientoEnum.INGRESO) {
                ingresos = suma;
            } else if (tipo == TipoMovimientoEnum.GASTO) {
                gastos = suma.abs();
            }
        }
        BigDecimal diferencia = ingresos.subtract(gastos);

        return new ResumenCuentaResponse(totalCuentas, ingresos, gastos, diferencia, cuentas.size());
    }

    public List<TransaccionEntity> getAllTransacciones(Long cuentaId, UserEntity user) {
        return cargarCuenta(cuentaId, user).getTransacciones();
    }

    public TransaccionEntity getTransaccion(Long cuentaId, Long transaccionId, UserEntity user) {
        cargarCuenta(cuentaId, user);
        TransaccionEntity transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaccion no encontrada con id " + transaccionId));
        verificarTransaccionDeCuenta(transaccion, cuentaId);
        return transaccion;
    }

    // ── Cuentas ──────────────────────────────────────────────────────────────

    public CuentaEntity addCuenta(UserEntity user, CuentaDTO cuentaDTO) {
        CuentaEntity cuenta = new CuentaEntity();
        cuenta.setUser(user);
        cuenta.setNombreCuenta(cuentaDTO.nombreCuenta());
        cuenta.setSaldoInicial(cuentaDTO.saldoInicial());
        CuentaEntity guardada = repository.save(cuenta);
        guardada.hidratarSaldo(BigDecimal.ZERO);
        return guardada;
    }

    /**
     * Edita nombre y saldo inicial. Ojo: {@code saldoInicial} es el saldo de
     * partida, no el actual; el actual sale de sumarle los movimientos.
     */
    public CuentaEntity updateCuenta(Long id, CuentaDTO cuentaDTO, UserEntity user) {
        CuentaEntity cuenta = cargarCuenta(id, user);
        cuenta.setNombreCuenta(cuentaDTO.nombreCuenta());
        cuenta.setSaldoInicial(cuentaDTO.saldoInicial());
        CuentaEntity guardada = repository.save(cuenta);
        guardada.hidratarSaldo(transaccionRepository.sumaDeCuenta(id));
        return guardada;
    }

    @Transactional
    public void deleteCuenta(Long id, UserEntity user) {
        CuentaEntity cuenta = cargarCuenta(id, user);
        if (transaccionRepository.existsByCuentaId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar una cuenta con transacciones asociadas. Elimina primero sus movimientos.");
        }
        if (transaccionRepository.existsByCuentaContrapartidaId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar una cuenta que interviene en transferencias. Elimina primero esos traspasos.");
        }
        repository.delete(cuenta);
    }

    // ── Transacciones ────────────────────────────────────────────────────────

    @Transactional
    public TransaccionEntity addTransaccionToCuenta(Long cuentaId, TransaccionDTO transaccionDTO, UserEntity user) {
        CuentaEntity cuenta = cargarCuenta(cuentaId, user);
        validarDTO(transaccionDTO, cuentaId);

        if (transaccionDTO.tipoMovimiento() == TipoMovimientoEnum.TRANSFERENCIA) {
            return crearTransferencia(cuenta, transaccionDTO, user);
        }

        TransaccionEntity transaccion = new TransaccionEntity();
        transaccion.setUser(user);
        transaccion.setCuenta(cuenta);
        aplicarDTO(transaccion, transaccionDTO, user);
        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public TransaccionEntity updateTransaccion(Long cuentaId, Long transaccionId, TransaccionDTO transaccionDTO, UserEntity user) {
        TransaccionEntity transaccion = getTransaccion(cuentaId, transaccionId, user);
        validarDTO(transaccionDTO, cuentaId);

        boolean era = transaccion.esTransferencia();
        boolean sera = transaccionDTO.tipoMovimiento() == TipoMovimientoEnum.TRANSFERENCIA;

        // Pasar de transferencia a movimiento normal (o al revés) cambia el
        // número de apuntes, así que se rehace en vez de parchearse.
        if (era != sera) {
            borrarApuntes(transaccion);
            return addTransaccionToCuenta(cuentaId, transaccionDTO, user);
        }

        if (sera) {
            return actualizarTransferencia(transaccion, transaccionDTO, user);
        }

        aplicarDTO(transaccion, transaccionDTO, user);
        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public void deleteTransaccion(Long cuentaId, Long transaccionId, UserEntity user) {
        borrarApuntes(getTransaccion(cuentaId, transaccionId, user));
    }

    // ── Transferencias ───────────────────────────────────────────────────────

    /**
     * Doble apunte: salida negativa en la cuenta origen y entrada positiva en la
     * de destino, ligados por el mismo {@code transferenciaId}. Así el saldo de
     * cada cuenta sigue siendo la simple suma de sus movimientos y el traspaso
     * no altera el patrimonio total.
     */
    private TransaccionEntity crearTransferencia(CuentaEntity origen, TransaccionDTO dto, UserEntity user) {
        CuentaEntity destino = cargarCuenta(dto.cuentaDestinoId(), user);
        String grupo = UUID.randomUUID().toString();
        BigDecimal magnitud = dto.importe().abs();

        TransaccionEntity salida = nuevoApunte(user, origen, destino, magnitud.negate(), grupo, dto);
        TransaccionEntity entrada = nuevoApunte(user, destino, origen, magnitud, grupo, dto);

        transaccionRepository.save(entrada);
        // Se devuelve el apunte de la cuenta desde la que se ha creado.
        return transaccionRepository.save(salida);
    }

    private TransaccionEntity nuevoApunte(UserEntity user, CuentaEntity cuenta, CuentaEntity contrapartida,
                                          BigDecimal importeConSigno, String grupo, TransaccionDTO dto) {
        TransaccionEntity apunte = new TransaccionEntity();
        apunte.setUser(user);
        apunte.setCuenta(cuenta);
        apunte.setCuentaContrapartida(contrapartida);
        apunte.setTransferenciaId(grupo);
        apunte.setCategoria(null);
        apunte.setImporte(importeConSigno);
        aplicarComunes(apunte, dto);
        return apunte;
    }

    /**
     * Edita los dos apuntes a la vez. El apunte sobre el que se edita conserva
     * su papel (si era la salida sigue siéndolo), y {@code cuentaDestinoId} se
     * interpreta siempre como "la otra cuenta"; así editar desde el lado
     * receptor nunca invierte el sentido del traspaso sin avisar.
     */
    private TransaccionEntity actualizarTransferencia(TransaccionEntity apunte, TransaccionDTO dto, UserEntity user) {
        TransaccionEntity contrario = transaccionRepository.findByTransferenciaId(apunte.getTransferenciaId()).stream()
                .filter(a -> !a.getId().equals(apunte.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "La transferencia no tiene apunte de contrapartida; elimínala y vuelve a crearla."));

        CuentaEntity otraCuenta = cargarCuenta(dto.cuentaDestinoId(), user);
        BigDecimal magnitud = dto.importe().abs();
        boolean apunteEsSalida = apunte.getImporte().signum() < 0;

        aplicarComunes(apunte, dto);
        apunte.setImporte(apunteEsSalida ? magnitud.negate() : magnitud);
        apunte.setCuentaContrapartida(otraCuenta);

        aplicarComunes(contrario, dto);
        contrario.setImporte(apunteEsSalida ? magnitud : magnitud.negate());
        contrario.setCuenta(otraCuenta);
        contrario.setCuentaContrapartida(apunte.getCuenta());

        transaccionRepository.save(contrario);
        return transaccionRepository.save(apunte);
    }

    /** Borra la transacción y, si es una transferencia, también su contrapartida. */
    private void borrarApuntes(TransaccionEntity transaccion) {
        if (transaccion.esTransferencia() && transaccion.getTransferenciaId() != null) {
            transaccionRepository.deleteAll(
                    transaccionRepository.findByTransferenciaId(transaccion.getTransferenciaId()));
            return;
        }
        transaccionRepository.delete(transaccion);
    }

    // ── Apoyo ────────────────────────────────────────────────────────────────

    /** Carga y comprueba propiedad, sin calcular el saldo (rutas de escritura). */
    private CuentaEntity cargarCuenta(Long id, UserEntity user) {
        CuentaEntity cuenta = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada con id " + id));
        verificarPropiedad(cuenta, user);
        return cuenta;
    }

    private void validarDTO(TransaccionDTO dto, Long cuentaOrigenId) {
        if (dto.tipoMovimiento() == TipoMovimientoEnum.TRANSFERENCIA) {
            if (dto.cuentaDestinoId() == null) {
                throw new IllegalArgumentException("Una transferencia necesita una cuenta de destino");
            }
            if (dto.cuentaDestinoId().equals(cuentaOrigenId)) {
                throw new IllegalArgumentException("La cuenta de origen y la de destino no pueden ser la misma");
            }
        } else if (dto.categoriaId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
    }

    private void aplicarComunes(TransaccionEntity transaccion, TransaccionDTO dto) {
        transaccion.setTipoMovimiento(dto.tipoMovimiento());
        transaccion.setDescripcion(dto.descripcion());
        transaccion.setFechaTransaccion(dto.fecha());
    }

    private void aplicarDTO(TransaccionEntity transaccion, TransaccionDTO transaccionDTO, UserEntity user) {
        aplicarComunes(transaccion, transaccionDTO);
        transaccion.setCategoria(resolverCategoria(transaccionDTO.categoriaId(), user));
        transaccion.setImporte(conSigno(transaccionDTO.importe(), transaccionDTO.tipoMovimiento()));
        transaccion.setTransferenciaId(null);
        transaccion.setCuentaContrapartida(null);
    }

    /** Importe con signo: negativo para gasto/inversión, positivo para ingreso. */
    private static BigDecimal conSigno(BigDecimal importe, TipoMovimientoEnum tipo) {
        if (importe == null) return BigDecimal.ZERO;
        BigDecimal magnitud = importe.abs();
        return tipo.restaDelSaldo() ? magnitud.negate() : magnitud;
    }

    private CategoriaEntity resolverCategoria(Long categoriaId, UserEntity user) {
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada con id " + categoriaId));
        if (!categoria.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("La categoria no pertenece al usuario");
        }
        return categoria;
    }

    private void verificarPropiedad(CuentaEntity cuenta, UserEntity user) {
        if (!cuenta.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No tienes acceso a esta cuenta");
        }
    }

    private void verificarTransaccionDeCuenta(TransaccionEntity transaccion, Long cuentaId) {
        if (!transaccion.getCuenta().getId().equals(cuentaId)) {
            throw new EntityNotFoundException("Transaccion no encontrada en esta cuenta");
        }
    }
}
