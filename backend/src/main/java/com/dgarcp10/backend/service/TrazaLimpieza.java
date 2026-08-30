package com.dgarcp10.backend.service;
import java.time.Instant;

import com.dgarcp10.backend.model.TipoTareaLimpieza;
public record TrazaLimpieza(
        Long id,
        Integer habitacionNumero,
        String habitacionTipo,
        TipoTareaLimpieza tipo,
        String completadoPor,
        Instant completadaEn,
        Instant creadoEn) {
}