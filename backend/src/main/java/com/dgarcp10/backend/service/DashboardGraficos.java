package com.dgarcp10.backend.service;
import java.util.List;
public record DashboardGraficos(
        int anio,
        int mes,
        List<OcupacionPunto> ocupacionAnual,
        List<OcupacionPunto> ocupacionMensual,
        List<GananciasPunto> gananciasAnuales,
        List<GananciasPunto> gananciasMensuales) {
}