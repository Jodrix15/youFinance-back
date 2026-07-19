package com.example.finanzas.service.impl;

import com.example.finanzas.model.PatrimonioSnapshotEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.repository.PatrimonioSnapshotRepository;
import com.example.finanzas.service.CuentaService;
import com.example.finanzas.service.DashboardService;
import com.example.finanzas.service.DeudaService;
import com.example.finanzas.service.InversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CuentaService cuentaService;
    private final InversionService inversionService;
    private final DeudaService deudaService;
    private final PatrimonioSnapshotRepository snapshotRepository;

    @Override
    public BigDecimal getPatrimonioNeto(UserEntity user) {
        return cuentaService.getImporteTotal(user)
                .add(inversionService.getImporteTotal(user))
                .subtract(deudaService.getImporteTotal(user));
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
}
