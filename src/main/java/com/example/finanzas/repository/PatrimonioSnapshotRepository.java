package com.example.finanzas.repository;

import com.example.finanzas.model.PatrimonioSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatrimonioSnapshotRepository extends JpaRepository<PatrimonioSnapshotEntity, Long> {

    List<PatrimonioSnapshotEntity> findByUserIdOrderByMesAsc(UUID userId);

    Optional<PatrimonioSnapshotEntity> findByUserIdAndMes(UUID userId, LocalDate mes);
}
