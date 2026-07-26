package com.example.finanzas.service.impl;
import com.example.finanzas.service.CategoriaService;

import com.example.finanzas.dto.categoria.CrearCategoria;
import com.example.finanzas.model.CategoriaEntity;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.model.enums.OrigenIngresoEnum;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import com.example.finanzas.repository.CategoriaRepository;
import com.example.finanzas.repository.GastoRecurrenteRepository;
import com.example.finanzas.repository.InversionRepository;
import com.example.finanzas.repository.TransaccionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repository;
    private final TransaccionRepository transaccionRepository;
    private final InversionRepository inversionRepository;
    private final GastoRecurrenteRepository gastoRecurrenteRepository;

    public CategoriaEntity crear(CrearCategoria dto, UserEntity user) {
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNombreCategoria(dto.nombre());
        categoria.setTipo(dto.tipo());
        categoria.setOrigenIngreso(resolverOrigen(dto));
        categoria.setUser(user);
        return repository.save(categoria);
    }

    public List<CategoriaEntity> listar(UserEntity user) {
        return repository.findByUserId(user.getId());
    }

    @Override
    @Transactional
    public CategoriaEntity actualizar(Long id, CrearCategoria dto, UserEntity user) {
        CategoriaEntity categoria = buscarPropia(id, user);
        // Cambiar el tipo de una categoría con movimientos rompería la coherencia
        // de los datos (un gasto pasaría a contarse como ingreso, etc.).
        if (categoria.getTipo() != dto.tipo() && estaEnUso(id)) {
            throw new IllegalStateException(
                    "No puedes cambiar el tipo de una categoría con movimientos asociados");
        }
        categoria.setNombreCategoria(dto.nombre());
        categoria.setTipo(dto.tipo());
        categoria.setOrigenIngreso(resolverOrigen(dto));
        return repository.save(categoria);
    }

    /**
     * La familia (origenIngreso) solo tiene sentido en categorías de ingreso:
     * es obligatoria cuando el tipo es INGRESO y se ignora (queda null) en el
     * resto. Cambiar solo la familia de una categoría en uso es seguro porque
     * no altera el signo del importe de sus movimientos.
     */
    private OrigenIngresoEnum resolverOrigen(CrearCategoria dto) {
        if (dto.tipo() == TipoMovimientoEnum.INGRESO) {
            if (dto.origenIngreso() == null) {
                throw new IllegalArgumentException(
                        "Una categoría de ingreso debe indicar su familia (Activo, Pasivo o Inversión)");
            }
            return dto.origenIngreso();
        }
        return null;
    }

    @Override
    @Transactional
    public void eliminar(Long id, UserEntity user) {
        CategoriaEntity categoria = buscarPropia(id, user);
        if (estaEnUso(id)) {
            throw new IllegalStateException(
                    "No puedes eliminar una categoría con movimientos asociados");
        }
        repository.delete(categoria);
    }

    private CategoriaEntity buscarPropia(Long id, UserEntity user) {
        return repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
    }

    private boolean estaEnUso(Long categoriaId) {
        return transaccionRepository.existsByCategoriaId(categoriaId)
                || inversionRepository.existsByCategoriaId(categoriaId)
                || gastoRecurrenteRepository.existsByCategoriaId(categoriaId);
    }
}
