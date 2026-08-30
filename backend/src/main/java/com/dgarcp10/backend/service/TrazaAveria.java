package com.dgarcp10.backend.service;
import java.time.Instant;

import com.dgarcp10.backend.model.EstadoAveria;
import com.dgarcp10.backend.model.GravedadAveria;
public record TrazaAveria(
        Long id,
        Integer habitacionNumero,
        String habitacionTipo,
        GravedadAveria gravedad,
        EstadoAveria estado,
        String reportadoPor,
        Instant creadoEn,
        String resueltaPor,
        Instant resueltaEn) {
}