package com.dgarcp10.backend.service;
import java.util.List;
public record DashboardSeguimiento(
        List<TrazaLimpieza> tareasLimpieza,
        List<TrazaAveria> averias,
        List<TrazaNotificacion> notificacionesConHabitacion,
        List<TrazaNotificacion> notificacionesSinHabitacion) {
}