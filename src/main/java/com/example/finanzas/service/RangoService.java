package com.example.finanzas.service;

import com.example.finanzas.dto.rango.RangoResponse;
import com.example.finanzas.model.UserEntity;

public interface RangoService {
    /** Rango actual del usuario y su progreso, según la XP de sus logros desbloqueados. */
    RangoResponse getRango(UserEntity user);
}
