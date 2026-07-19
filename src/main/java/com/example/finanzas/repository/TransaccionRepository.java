package com.example.finanzas.repository;

import com.example.finanzas.model.TransaccionEntity;
import com.example.finanzas.model.enums.TipoMovimientoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {

    List<TransaccionEntity> findByUserId(UUID userId);

    boolean existsByCategoriaId(Long categoriaId);

    /** Todas las transacciones del usuario en una sola consulta (evita el N+1). */
    @Query("select t from TransaccionEntity t " +
            "join fetch t.cuenta left join fetch t.categoria " +
            "where t.user.id = :userId")
    List<TransaccionEntity> findAllByUserIdFetch(@Param("userId") UUID userId);

    /** Búsqueda paginada/filtrada. El texto :q llega ya en minúsculas y con %..%. */
    @Query("select t from TransaccionEntity t where t.user.id = :userId " +
            "and (:tipo is null or t.tipoMovimiento = :tipo) " +
            "and (:cuentaId is null or t.cuenta.id = :cuentaId) " +
            "and (:anio is null or year(t.fechaTransaccion) = :anio) " +
            "and (:mes is null or month(t.fechaTransaccion) = :mes) " +
            "and (:q is null or lower(t.descripcion) like :q " +
            "or lower(t.categoria.nombreCategoria) like :q)")
    Page<TransaccionEntity> buscar(@Param("userId") UUID userId,
                                   @Param("tipo") TipoMovimientoEnum tipo,
                                   @Param("cuentaId") Long cuentaId,
                                   @Param("anio") Integer anio,
                                   @Param("mes") Integer mes,
                                   @Param("q") String q,
                                   Pageable pageable);

    /** Totales por tipo del conjunto filtrado (para los KPIs). */
    @Query("select t.tipoMovimiento, coalesce(sum(t.importe), 0) from TransaccionEntity t " +
            "where t.user.id = :userId " +
            "and (:tipo is null or t.tipoMovimiento = :tipo) " +
            "and (:cuentaId is null or t.cuenta.id = :cuentaId) " +
            "and (:anio is null or year(t.fechaTransaccion) = :anio) " +
            "and (:mes is null or month(t.fechaTransaccion) = :mes) " +
            "and (:q is null or lower(t.descripcion) like :q " +
            "or lower(t.categoria.nombreCategoria) like :q) " +
            "group by t.tipoMovimiento")
    List<Object[]> resumenPorTipo(@Param("userId") UUID userId,
                                  @Param("tipo") TipoMovimientoEnum tipo,
                                  @Param("cuentaId") Long cuentaId,
                                  @Param("anio") Integer anio,
                                  @Param("mes") Integer mes,
                                  @Param("q") String q);
}
