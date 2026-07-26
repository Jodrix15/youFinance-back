package com.example.finanzas.service;

import com.example.finanzas.dto.logro.LogroResponse;
import com.example.finanzas.model.UserEntity;

import java.util.List;

public interface LogroService {
    /** Evalúa los logros del usuario, desbloquea los recién cumplidos y devuelve todos. */
    List<LogroResponse> getLogros(UserEntity user);
}
