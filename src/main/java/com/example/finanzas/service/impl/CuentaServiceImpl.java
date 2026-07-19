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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository repository;
    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;

    public CuentaEntity getCuenta(Long id, UserEntity user) {
        CuentaEntity cuenta = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada con id " + id));
        verificarPropiedad(cuenta, user);
        return cuenta;
    }

    public List<CuentaEntity> getAllCuentas(UserEntity user) {
        return repository.findByUserId(user.getId());
    }

    public CuentaEntity addCuenta(UserEntity user, CuentaDTO cuentaDTO) {
        CuentaEntity cuenta = new CuentaEntity();
        cuenta.setUser(user);
        cuenta.setNombreCuenta(cuentaDTO.nombreCuenta());
        cuenta.setImporte(cuentaDTO.importe());
        return repository.save(cuenta);
    }

    public CuentaEntity updateCuenta(Long id, CuentaDTO cuentaDTO, UserEntity user) {
        CuentaEntity cuenta = getCuenta(id, user);
        cuenta.setNombreCuenta(cuentaDTO.nombreCuenta());
        cuenta.setImporte(cuentaDTO.importe());
        return repository.save(cuenta);
    }

    public List<TransaccionEntity> getAllTransacciones(Long cuentaId, UserEntity user) {
        return getCuenta(cuentaId, user).getTransacciones();
    }

    public BigDecimal getImporteTotal(UserEntity user) {
        return getAllCuentas(user).stream()
                .map(CuentaEntity::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ResumenCuentaResponse getResumen(UserEntity user, Integer anio, Integer mes) {
        List<CuentaEntity> cuentas = getAllCuentas(user);
        BigDecimal totalCuentas = cuentas.stream()
                .map(CuentaEntity::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;
        // Reutiliza el agregado por tipo del histórico (sin filtrar por cuenta ni texto).
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

    public TransaccionEntity getTransaccion(Long cuentaId, Long transaccionId, UserEntity user) {
        getCuenta(cuentaId, user);
        TransaccionEntity transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaccion no encontrada con id " + transaccionId));
        verificarTransaccionDeCuenta(transaccion, cuentaId);
        return transaccion;
    }

    @Transactional
    public TransaccionEntity addTransaccionToCuenta(Long cuentaId, TransaccionDTO transaccionDTO, UserEntity user) {
        CuentaEntity cuenta = getCuenta(cuentaId, user);

        TransaccionEntity transaccion = new TransaccionEntity();
        transaccion.setUser(user);
        transaccion.setCuenta(cuenta);
        aplicarDTO(transaccion, transaccionDTO, user);
        cuenta.aplicarTransaccion(transaccion);
        repository.save(cuenta);
        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public TransaccionEntity updateTransaccion(Long cuentaId, Long transaccionId, TransaccionDTO transaccionDTO, UserEntity user) {
        TransaccionEntity transaccion = getTransaccion(cuentaId, transaccionId, user);
        CuentaEntity cuenta = transaccion.getCuenta();
        aplicarDTO(transaccion, transaccionDTO, user);
        cuenta.aplicarTransaccion(transaccion);

        repository.save(cuenta);
        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public void deleteTransaccion(Long cuentaId, Long transaccionId, UserEntity user) {
        TransaccionEntity transaccion = getTransaccion(cuentaId, transaccionId, user);
        CuentaEntity cuenta = transaccion.getCuenta();
        // Revierte el efecto de la transacción en el saldo antes de borrarla.
        cuenta.setImporte(cuenta.getImporte().subtract(transaccion.getImporteConSigno()));
        repository.save(cuenta);
        transaccionRepository.delete(transaccion);
    }

    private void aplicarDTO(TransaccionEntity transaccion, TransaccionDTO transaccionDTO, UserEntity user) {
        transaccion.setCategoria(resolverCategoria(transaccionDTO.categoriaId(), user));
        transaccion.setTipoMovimiento(transaccionDTO.tipoMovimiento());
        transaccion.setImporte(conSigno(transaccionDTO.importe(), transaccionDTO.tipoMovimiento()));
        transaccion.setDescripcion(transaccionDTO.descripcion());
        transaccion.setFechaTransaccion(transaccionDTO.fecha());
    }

    /** Importe con signo: negativo para gasto/inversión, positivo para ingreso. */
    private static BigDecimal conSigno(BigDecimal importe, TipoMovimientoEnum tipo) {
        if (importe == null) return BigDecimal.ZERO;
        BigDecimal magnitud = importe.abs();
        boolean resta = tipo == TipoMovimientoEnum.GASTO || tipo == TipoMovimientoEnum.INVERSION;
        return resta ? magnitud.negate() : magnitud;
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