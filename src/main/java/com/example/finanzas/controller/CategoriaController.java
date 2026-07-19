package com.example.finanzas.controller;

import com.example.finanzas.dto.categoria.CategoriaResponse;
import com.example.finanzas.dto.categoria.CrearCategoria;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categoria")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar(@AuthenticationPrincipal UserEntity user) {
        List<CategoriaResponse> respuesta = service.listar(user).stream()
                .map(CategoriaResponse::from)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CrearCategoria dto,
                                                   @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoriaResponse.from(service.crear(dto, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CrearCategoria dto,
                                                        @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(CategoriaResponse.from(service.actualizar(id, dto, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UserEntity user) {
        service.eliminar(id, user);
        return ResponseEntity.noContent().build();
    }
}
   