package com.example.finanzas.service.impl;
import com.example.finanzas.service.DeudaService;

import com.example.finanzas.dto.Deuda.DeudaDTO;
import com.example.finanzas.dto.Deuda.ResumenDeudaResponse;
import com.example.finanzas.model.DeudaEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.repository.DeudaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeudaServiceImpl implements DeudaService {

    private final DeudaRepository repository;

    public DeudaEntity getDeuda(Long id, UserEntity user) {
        DeudaEntity deuda = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada con id " + id));
        verificarPropiedad(deuda, user);
        return deuda;
    }

    public List<DeudaEntity> getAllDeudas(UserEntity user) {
        return repository.findByUserId(user.getId());
    }

    public DeudaEntity crear(DeudaDTO deudaDTO, UserEntity user){
        DeudaEntity deuda = new DeudaEntity();
        aplicarDTO(deuda, deudaDTO);
        deuda.setUser(user);
        return repository.save(deuda);
    }

    public BigDecimal getImporteTotal(UserEntity user) {
        BigDecimal importe = BigDecimal.ZERO;
        List<DeudaEntity> deudas = getAllDeudas(user);
        for (DeudaEntity deuda : deudas) {
            importe = importe.add(deuda.getImporteTotal());
        }
        return importe;
    }

    public ResumenDeudaResponse getResumen(UserEntity user) {
        List<DeudaEntity> deudas = getAllDeudas(user);
        BigDecimal totalPendiente = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal totalConIntereses = BigDecimal.ZERO;
        BigDecimal gastoMensual = BigDecimal.ZERO;
        LocalDate hoy = LocalDate.now();

        for (DeudaEntity deuda : deudas) {
            BigDecimal pendiente = deuda.getCantidadPendiente();
            if (pendiente != null) {
                totalPendiente = totalPendiente.add(pendiente);
            }
            if (deuda.getCantidadPagada() != null) {
                totalPagado = totalPagado.add(deuda.getCantidadPagada());
            }
            BigDecimal total = deuda.getImporteTotal();
            if (total != null) {
                totalConIntereses = totalConIntereses.add(total);
            }
            // Gasto mensual estimado: pendiente repartido en los meses que faltan
            // hasta el vencimiento (solo deudas con fecha futura).
            long meses = mesesHasta(hoy, deuda.getFechaVencimiento());
            if (meses > 0 && pendiente != null) {
                gastoMensual = gastoMensual.add(
                        pendiente.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP));
            }
        }

        return new ResumenDeudaResponse(
                totalPendiente, totalPagado, totalConIntereses, gastoMensual, deudas.size());
    }

    /** Meses enteros desde hoy hasta la fecha de vencimiento (0 si no hay fecha o ya pasó). */
    private static long mesesHasta(LocalDate hoy, LocalDate vencimiento) {
        if (vencimiento == null) {
            return 0;
        }
        return (vencimiento.getYear() - hoy.getYear()) * 12L
                + (vencimiento.getMonthValue() - hoy.getMonthValue());
    }

    public DeudaEntity update(Long id, DeudaDTO deudaDTO, UserEntity user){
        DeudaEntity deuda = getDeuda(id, user);
        aplicarDTO(deuda, deudaDTO);
        return repository.save(deuda);
    }

    public void remove(Long id, UserEntity user) {
        repository.delete(getDeuda(id, user));
    }

    private void aplicarDTO(DeudaEntity deuda, DeudaDTO deudaDTO) {
        deuda.setNombreDeuda(deudaDTO.nombreDeuda());
        deuda.setImporte(deudaDTO.importe());
        deuda.setCantidadPagada(deudaDTO.cantidadPagada());
        deuda.setAcreedor(deudaDTO.acreedor());
        deuda.setInteres(deudaDTO.interes());
        deuda.setFrecuencia(deudaDTO.frecuencia());
        deuda.setCuota(deudaDTO.cuota());
        deuda.setFechaVencimiento(deudaDTO.fechaVencimiento());
    }

    private void verificarPropiedad(DeudaEntity deuda, UserEntity user) {
        if (!deuda.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No tienes acceso a esta deuda");
        }
    }
}
