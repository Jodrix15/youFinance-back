package com.example.finanzas.service.impl;

import com.example.finanzas.dto.presupuesto.PartidaDTO;
import com.example.finanzas.dto.presupuesto.PresupuestoDTO;
import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.PresupuestoEntity;
import com.example.finanzas.model.PresupuestoPartidaEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.PeriodoPresupuestoEnum;
import com.example.finanzas.repository.CategoriaRepository;
import com.example.finanzas.repository.PresupuestoRepository;
import com.example.finanzas.service.PresupuestoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PresupuestoServiceImpl implements PresupuestoService {

    private final PresupuestoRepository repository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public List<PresupuestoEntity> getAll(UserEntity user) {
        return repository.findByUserIdFetch(user.getId());
    }

    @Override
    public PresupuestoEntity get(Long id, UserEntity user) {
        PresupuestoEntity presupuesto = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado con id " + id));
        verificarPropiedad(presupuesto, user);
        return presupuesto;
    }

    @Override
    @Transactional
    public PresupuestoEntity crear(PresupuestoDTO dto, UserEntity user) {
        PresupuestoEntity presupuesto = new PresupuestoEntity();
        presupuesto.setUser(user);
        aplicarDTO(presupuesto, dto, user);
        return repository.save(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoEntity update(Long id, PresupuestoDTO dto, UserEntity user) {
        PresupuestoEntity presupuesto = get(id, user);
        presupuesto.clearPartidas();
        aplicarDTO(presupuesto, dto, user);
        return repository.save(presupuesto);
    }

    @Override
    @Transactional
    public void remove(Long id, UserEntity user) {
        repository.delete(get(id, user));
    }

    private void aplicarDTO(PresupuestoEntity presupuesto, PresupuestoDTO dto, UserEntity user) {
        presupuesto.setNombre(dto.nombre());
        presupuesto.setPeriodo(dto.periodo());
        presupuesto.setAnio(dto.anio());
        presupuesto.setMes(dto.mes());
        presupuesto.setSemana(dto.periodo() == PeriodoPresupuestoEnum.SEMANAL ? dto.semana() : null);
        presupuesto.setCantidadBase(dto.cantidadBase());
        presupuesto.setDescontarGastosFijos(dto.descontarGastosFijos());

        if (dto.partidas() != null) {
            for (PartidaDTO partidaDTO : dto.partidas()) {
                PresupuestoPartidaEntity partida = new PresupuestoPartidaEntity();
                partida.setImporte(partidaDTO.importe() != null ? partidaDTO.importe() : BigDecimal.ZERO);
                partida.setNombre(partidaDTO.nombre());
                if (partidaDTO.categoriaId() != null) {
                    partida.setCategoria(resolverCategoria(partidaDTO.categoriaId(), user));
                }
                presupuesto.addPartida(partida);
            }
        }
    }

    private CategoriaEntity resolverCategoria(Long categoriaId, UserEntity user) {
        return categoriaRepository.findByIdAndUserId(categoriaId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría no encontrada con id " + categoriaId));
    }

    private void verificarPropiedad(PresupuestoEntity presupuesto, UserEntity user) {
        if (!presupuesto.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No tienes acceso a este presupuesto");
        }
    }
}
