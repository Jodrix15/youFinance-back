package com.example.finanzas.controller;

import com.example.finanzas.dto.logro.LogroResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.LogroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logros")
@RequiredArgsConstructor
public class LogroController {

    private final LogroService logroService;

    /** Evalúa y devuelve los logros del usuario (desbloquea los recién cumplidos). */
    @GetMapping
    public ResponseEntity<List<LogroResponse>> getLogros(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(logroService.getLogros(user));
    }
}
