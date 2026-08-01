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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    /**
     * Paleta de marca, en el mismo orden que la del front ({@code lib/chartSetup.ts}).
     * Es la lista de colores entre los que se elige cuando el usuario no indica
     * ninguno, y también la que se le ofrece en el formulario.
     */
    private static final List<String> PALETA = List.of(
            "#2f81f7", // azul
            "#1d9e75", // verde
            "#d29922", // ámbar
            "#8b7ec8", // morado
            "#d85a30", // coral
            "#d4537e", // rosa
            "#6e7681"  // gris
    );

    private final CategoriaRepository repository;
    private final TransaccionRepository transaccionRepository;
    private final InversionRepository inversionRepository;
    private final GastoRecurrenteRepository gastoRecurrenteRepository;

    public CategoriaEntity crear(CrearCategoria dto, UserEntity user) {
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNombreCategoria(dto.nombre());
        categoria.setTipo(dto.tipo());
        categoria.setOrigenIngreso(resolverOrigen(dto));
        categoria.setColor(resolverColor(dto.color(), dto.tipo(), user, null));
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
        // Sin color en la petición se vuelve al reparto automático: es lo que
        // significa la opción "automático" del formulario.
        categoria.setColor(resolverColor(dto.color(), dto.tipo(), user, categoria.getId()));
        return repository.save(categoria);
    }

    /**
     * Color definitivo de la categoría. Si el usuario elige uno se respeta tal
     * cual (normalizado a minúsculas); si no indica ninguno se toma el primer
     * color de la paleta que no esté ya usado por otra categoría del mismo
     * tipo, de forma que dos categorías hermanas nunca nacen con el mismo
     * color. Si la paleta se ha agotado se reparte cíclicamente.
     *
     * @param idActual id de la categoría que se está editando (null al crear):
     *                 su propio color no cuenta como "ocupado".
     */
    private String resolverColor(String colorElegido, TipoMovimientoEnum tipo, UserEntity user, Long idActual) {
        if (colorElegido != null && !colorElegido.isBlank()) {
            return colorElegido.trim().toLowerCase(Locale.ROOT);
        }

        List<CategoriaEntity> hermanas = repository.findByUserIdAndTipo(user.getId(), tipo);
        Set<String> ocupados = hermanas.stream()
                .filter(c -> !c.getId().equals(idActual))
                .map(CategoriaEntity::getColor)
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return PALETA.stream()
                .filter(c -> !ocupados.contains(c))
                .findFirst()
                // Con más categorías que colores ya no hay ninguno libre: se
                // reparte la paleta en ciclo para no dejar la categoría sin color.
                .orElseGet(() -> PALETA.get(ocupados.size() % PALETA.size()));
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
