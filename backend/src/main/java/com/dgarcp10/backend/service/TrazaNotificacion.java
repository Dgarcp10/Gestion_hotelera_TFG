package com.dgarcp10.backend.service;
import java.time.Instant;
public record TrazaNotificacion(
        Long id,
        String tipo,
        String mensaje,
        Long habitacionId,
        Integer habitacionNumero,
        Instant creadoEn) {
}