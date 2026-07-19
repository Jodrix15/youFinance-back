package com.example.finanzas.dto.dashboard;

import com.example.finanzas.model.PatrimonioSnapshotEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PatrimonioSnapshotResponse(
        LocalDate mes,
        BigDecimal patrimonioNeto,
        BigDecimal cuentas,
        BigDecimal inversiones,
        BigDecimal deudas
) {
    public static PatrimonioSnapshotResponse from(PatrimonioSnapshotEntity s) {
        return new PatrimonioSnapshotResponse(
                s.getMes(),
                s.getPatrimonioNeto(),
                s.getTotalCuentas(),
                s.getTotalInversiones(),
                s.getTotalDeudas()
        );
    }
}
