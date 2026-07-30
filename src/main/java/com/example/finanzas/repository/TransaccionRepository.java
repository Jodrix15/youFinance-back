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

    boolean existsByCuentaId(Long cuentaId);

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

    /** Totales por mes y tipo de un año (flujo de caja del dashboard). */
    @Query("select month(t.fechaTransaccion), t.tipoMovimiento, coalesce(sum(t.importe), 0) " +
            "from TransaccionEntity t " +
            "where t.user.id = :userId and year(t.fechaTransaccion) = :anio " +
            "group by month(t.fechaTransaccion), t.tipoMovimiento")
    List<Object[]> totalesPorMesYTipo(@Param("userId") UUID userId, @Param("anio") Integer anio);

    /** Total de gastos por categoría, con su color (widget de gastos por categoría). */
    @Query("select coalesce(t.categoria.nombreCategoria, 'Otros'), t.categoria.color, coalesce(sum(abs(t.importe)), 0) " +
            "from TransaccionEntity t " +
            "where t.user.id = :userId and t.tipoMovimiento = com.example.finanzas.model.enums.TipoMovimientoEnum.GASTO " +
            "group by t.categoria.nombreCategoria, t.categoria.color " +
            "order by sum(abs(t.importe)) desc")
    List<Object[]> gastosPorCategoria(@Param("userId") UUID userId);

    /**
     * Total de ingresos agrupados por familia (origenIngreso) de la categoría.
     * Los ingresos se guardan con importe positivo. Filtra opcionalmente por año/mes.
     * La familia puede venir null en categorías de ingreso aún sin clasificar.
     */
    @Query("select c.origenIngreso, coalesce(sum(t.importe), 0) " +
            "from TransaccionEntity t join t.categoria c " +
            "where t.user.id = :userId " +
            "and t.tipoMovimiento = com.example.finanzas.model.enums.TipoMovimientoEnum.INGRESO " +
            "and (:anio is null or year(t.fechaTransaccion) = :anio) " +
            "and (:mes is null or month(t.fechaTransaccion) = :mes) " +
            "group by c.origenIngreso")
    List<Object[]> ingresosPorFamilia(@Param("userId") UUID userId,
                                      @Param("anio") Integer anio,
                                      @Param("mes") Integer mes);

    /**
     * Ingresos por año, mes y familia de la categoría (para la curva de
     * evolución y su desglose). Devuelve solo los meses con ingresos; los
     * huecos se rellenan a cero en el servicio. La familia puede ser null:
     * categorías todavía sin clasificar.
     */
    @Query("select year(t.fechaTransaccion), month(t.fechaTransaccion), c.origenIngreso, coalesce(sum(t.importe), 0) " +
            "from TransaccionEntity t join t.categoria c " +
            "where t.user.id = :userId " +
            "and t.tipoMovimiento = com.example.finanzas.model.enums.TipoMovimientoEnum.INGRESO " +
            "group by year(t.fechaTransaccion), month(t.fechaTransaccion), c.origenIngreso " +
            "order by year(t.fechaTransaccion), month(t.fechaTransaccion)")
    List<Object[]> ingresosPorMesYFamilia(@Param("userId") UUID userId);

    /** Total de ingresos por categoría (nombre + familia + color), de mayor a menor. */
    @Query("select c.nombreCategoria, c.origenIngreso, c.color, coalesce(sum(t.importe), 0) " +
            "from TransaccionEntity t join t.categoria c " +
            "where t.user.id = :userId " +
            "and t.tipoMovimiento = com.example.finanzas.model.enums.TipoMovimientoEnum.INGRESO " +
            "and (:anio is null or year(t.fechaTransaccion) = :anio) " +
            "and (:mes is null or month(t.fechaTransaccion) = :mes) " +
            "group by c.nombreCategoria, c.origenIngreso, c.color " +
            "order by sum(t.importe) desc")
    List<Object[]> ingresosPorCategoria(@Param("userId") UUID userId,
                                        @Param("anio") Integer anio,
                                        @Param("mes") Integer mes);
}
