package com.example.finanzas.scheduler;

import com.example.finanzas.model.UserEntity;
import com.example.finanzas.repository.UserRepository;
import com.example.finanzas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Respaldo automático: el día 1 de cada mes congela el patrimonio de todos
 * los usuarios. El upsert del mes en curso también ocurre de forma perezosa
 * cada vez que el usuario abre el dashboard, así que esta tarea solo asegura
 * que los meses se registran aunque nadie entre.
 */
@Component
@RequiredArgsConstructor
public class PatrimonioSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(PatrimonioSnapshotScheduler.class);

    private final UserRepository userRepository;
    private final DashboardService dashboardService;

    // Segundo minuto hora díaDelMes mes díaSemana → 03:00 del día 1 de cada mes.
    @Scheduled(cron = "0 0 3 1 * *")
    public void capturarSnapshotsMensuales() {
        for (UserEntity user : userRepository.findAll()) {
            try {
                dashboardService.capturarSnapshot(user);
            } catch (Exception e) {
                log.error("No se pudo guardar el snapshot de patrimonio del usuario {}", user.getId(), e);
            }
        }
    }
}
