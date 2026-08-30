package com.dgarcp10.backend.controller;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.service.DashboardGraficos;
import com.dgarcp10.backend.service.DashboardSeguimiento;
import com.dgarcp10.backend.service.DashboardService;
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('JEFE')")
public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping("/graficos")
    public DashboardGraficos graficos(@RequestParam(required = false) Integer anio,
                                      @RequestParam(required = false) Integer mes) {
        int a = anio != null ? anio : LocalDate.now().getYear();
        int m = mes != null ? mes : LocalDate.now().getMonthValue();
        return dashboardService.resumenGraficos(a, m);
    }
    @GetMapping("/seguimiento")
    public DashboardSeguimiento seguimiento(@RequestParam LocalDate desde,
                                            @RequestParam LocalDate hasta,
                                            @RequestParam(required = false) Long tipoHabitacionId,
                                            @RequestParam(required = false) Integer numero) {
        return dashboardService.seguimiento(desde, hasta, tipoHabitacionId, numero);
    }
}