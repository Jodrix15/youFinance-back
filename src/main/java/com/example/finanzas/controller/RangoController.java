package com.example.finanzas.controller;

import com.example.finanzas.dto.rango.RangoResponse;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.RangoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rango")
@RequiredArgsConstructor
public class RangoController {

    private final RangoService rangoService;

    /** Rango actual del usuario y su progreso hacia el siguiente. */
    @GetMapping
    public ResponseEntity<RangoResponse> getRango(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(rangoService.getRango(user));
    }
}
