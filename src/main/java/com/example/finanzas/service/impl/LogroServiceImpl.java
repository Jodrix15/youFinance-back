package com.example.finanzas.service.impl;

import com.example.finanzas.dto.logro.LogroResponse;
import com.example.finanzas.model.DeudaEntity;
import com.example.finanzas.model.LogroUsuarioEntity;
import com.example.finanzas.model.PatrimonioSnapshotEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.LogroEnum;
import com.example.finanzas.model.logro.EvaluacionLogro;
import com.example.finanzas.model.logro.LogroContext;
import com.example.finanzas.repository.LogroUsuarioRepository;
import com.example.finanzas.repository.PresupuestoRepository;
import com.example.finanzas.repository.TransaccionRepository;
import com.example.finanzas.service.CuentaService;
import com.example.finanzas.service.DashboardService;
import com.example.finanzas.service.DeudaService;
import com.example.finanzas.service.InversionService;
import com.example.finanzas.service.LogroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogroServiceImpl implements LogroService {

    private final CuentaService cuentaService;
    private final InversionService inversionService;
    private final DeudaService deudaService;
    private final DashboardService dashboardService;
    private final PresupuestoRepository presupuestoRepository;
    private final TransaccionRepository transaccionRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;

    @Override
    @Transactional
    public List<LogroResponse> getLogros(UserEntity user) {
        LogroContext contexto = construirContexto(user);

        Map<String, LogroUsuarioEntity> desbloqueados = logroUsuarioRepository.findByUserId(user.getId()).stream()
                .collect(Collectors.toMap(LogroUsuarioEntity::getCodigo, l -> l, (a, b) -> a));

        List<LogroResponse> respuesta = new ArrayList<>();
        for (LogroEnum logro : LogroEnum.values()) {
            EvaluacionLogro ev = logro.evaluar(contexto);
            LogroUsuarioEntity ya = desbloqueados.get(logro.name());

            boolean nuevo = false;
            Instant fecha = ya != null ? ya.getFechaDesbloqueo() : null;

            // Se desbloquea la primera vez que se cumple; una vez logrado, permanece.
            if (ya == null && ev.cumplido()) {
                LogroUsuarioEntity nuevoLogro = new LogroUsuarioEntity();
                nuevoLogro.setUser(user);
                nuevoLogro.setCodigo(logro.name());
                nuevoLogro.setFechaDesbloqueo(Instant.now());
                logroUsuarioRepository.save(nuevoLogro);
                fecha = nuevoLogro.getFechaDesbloqueo();
                nuevo = true;
            }

            boolean desbloqueado = ya != null || ev.cumplido();
            respuesta.add(new LogroResponse(
                    logro.name(), logro.getNombre(), logro.getDescripcion(), logro.getIcono(),
                    desbloqueado, fecha, ev.actual(), ev.objetivo(), nuevo));
        }
        return respuesta;
    }

    private LogroContext construirContexto(UserEntity user) {
        int numeroCuentas = cuentaService.getAllCuentas(user).size();
        long numeroTransacciones = transaccionRepository.findByUserId(user.getId()).size();

        var inversiones = inversionService.getAllInversiones(user);
        int categoriasInversion = (int) inversiones.stream()
                .map(i -> i.getCategoria() != null ? i.getCategoria().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        var deudas = deudaService.getAllDeudas(user);
        boolean todasDeudasPagadas = !deudas.isEmpty() && deudas.stream()
                .allMatch(this::deudaSaldada);

        BigDecimal patrimonioNeto = dashboardService.getPatrimonioNeto(user);
        int racha = maxRachaCrecimiento(dashboardService.getHistorico(user));

        boolean algunMesEnVerde = dashboardService.getFlujoCaja(user, LocalDate.now().getYear()).stream()
                .anyMatch(m -> m.ingresos().compareTo(m.gastos()) > 0);

        int numeroPresupuestos = presupuestoRepository.findByUserId(user.getId()).size();

        return new LogroContext(
                numeroCuentas, numeroTransacciones, inversiones.size(), categoriasInversion,
                deudas.size(), todasDeudasPagadas, patrimonioNeto, racha, algunMesEnVerde, numeroPresupuestos);
    }

    private boolean deudaSaldada(DeudaEntity d) {
        BigDecimal pendiente = d.getCantidadPendiente();
        return pendiente == null || pendiente.signum() <= 0;
    }

    /** Máximo número de subidas consecutivas del patrimonio en el histórico (orden asc por mes). */
    private int maxRachaCrecimiento(List<PatrimonioSnapshotEntity> snapshots) {
        int max = 0;
        int racha = 0;
        BigDecimal previo = null;
        for (PatrimonioSnapshotEntity s : snapshots) {
            if (previo != null && s.getPatrimonioNeto().compareTo(previo) > 0) {
                racha++;
                max = Math.max(max, racha);
            } else {
                racha = 0;
            }
            previo = s.getPatrimonioNeto();
        }
        return max;
    }
}
