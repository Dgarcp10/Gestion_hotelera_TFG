package com.dgarcp10.backend.controller;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.BloqueoHabitacion;
import com.dgarcp10.backend.model.EstadoBloqueo;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;
import com.dgarcp10.backend.service.BloqueoService;
@RestController
@RequestMapping("/api/bloqueos")
@PreAuthorize("hasAnyRole('LIMPIEZA', 'MANTENIMIENTO', 'RECEPCION', 'JEFE')")
public class BloqueoHabitacionController {
    private final BloqueoService bloqueoService;
    private final UsuarioRepository usuarioRepo;
    public BloqueoHabitacionController(BloqueoService bloqueoService, UsuarioRepository usuarioRepo) {
        this.bloqueoService = bloqueoService;
        this.usuarioRepo = usuarioRepo;
    }
    @GetMapping
    public List<BloqueoHabitacion> listar(@RequestParam(required = false) EstadoBloqueo estado) {
        return bloqueoService.listar(estado);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueoHabitacion crear(@RequestParam Long habitacionId,
                                   @RequestParam LocalDate fechaInicio,
                                   @RequestParam LocalDate fechaFin,
                                   @RequestParam String motivo,
                                   @RequestParam(required = false) Boolean confirmarSinReubicacion,
                                   Authentication auth) {
        Usuario creadoPor = usuarioRepo.findByUsername((String) auth.getPrincipal()).orElseThrow();
        return bloqueoService.crear(habitacionId, fechaInicio, fechaFin, motivo, confirmarSinReubicacion, creadoPor);
    }
    @PostMapping("/{id}/cancelar")
    public BloqueoHabitacion cancelar(@PathVariable Long id) {
        return bloqueoService.cancelar(id);
    }
}