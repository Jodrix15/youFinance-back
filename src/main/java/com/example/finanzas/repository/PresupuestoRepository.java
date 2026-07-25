package com.example.finanzas.repository;

import com.example.finanzas.model.PresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PresupuestoRepository extends JpaRepository<PresupuestoEntity, Long> {

    /** Presupuestos del usuario con sus partidas (evita el N+1), más recientes primero. */
    @Query("select distinct p from PresupuestoEntity p " +
            "left join fetch p.partidas pa left join fetch pa.categoria " +
            "where p.user.id = :userId " +
            "order by p.anio desc, p.mes desc, p.semana desc, p.id desc")
    List<PresupuestoEntity> findByUserIdFetch(@Param("userId") UUID userId);

    List<PresupuestoEntity> findByUserId(UUID userId);
}
