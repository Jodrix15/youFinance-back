package com.example.finanzas.service.impl;

import com.example.finanzas.dto.rango.RangoResponse;
import com.example.finanzas.model.LogroUsuarioEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.LogroEnum;
import com.example.finanzas.model.enums.RangoEnum;
import com.example.finanzas.repository.LogroUsuarioRepository;
import com.example.finanzas.service.RangoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RangoServiceImpl implements RangoService {

    private final LogroUsuarioRepository logroUsuarioRepository;

    @Override
    public RangoResponse getRango(UserEntity user) {
        int xpTotal = experienciaTotal(user);

        RangoEnum rango = RangoEnum.desdeXp(xpTotal);
        RangoEnum siguiente = rango.siguiente();

        Integer xpSiguiente = siguiente != null ? siguiente.getXpMinimo() : null;
        int progreso = calcularProgreso(xpTotal, rango, siguiente);

        return new RangoResponse(
                rango.getNivel(),
                rango.getNombre(),
                xpTotal,
                rango.getXpMinimo(),
                xpSiguiente,
                progreso
        );
    }

    /** Suma la XP de los logros desbloqueados por el usuario. */
    private int experienciaTotal(UserEntity user) {
        List<LogroUsuarioEntity> desbloqueados = logroUsuarioRepository.findByUserId(user.getId());
        int total = 0;
        for (LogroUsuarioEntity logro : desbloqueados) {
            try {
                total += LogroEnum.valueOf(logro.getCodigo()).getExperiencia();
            } catch (IllegalArgumentException ignore) {
                // Un código antiguo que ya no existe en el enum no suma XP.
            }
        }
        return total;
    }

    /** Porcentaje (0-100) avanzado dentro del rango actual. En el máximo, 100. */
    private int calcularProgreso(int xpTotal, RangoEnum rango, RangoEnum siguiente) {
        if (siguiente == null) {
            return 100;
        }
        int base = rango.getXpMinimo();
        int tramo = siguiente.getXpMinimo() - base;
        if (tramo <= 0) {
            return 100;
        }
        int avance = Math.round((xpTotal - base) * 100f / tramo);
        return Math.max(0, Math.min(100, avance));
    }
}
