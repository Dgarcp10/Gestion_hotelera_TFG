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

import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.repository.UsuarioRepository;
import com.dgarcp10.backend.service.ReservaService;
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepo;
    public ReservaController(ReservaService reservaService,
                             UsuarioRepository usuarioRepo) {
        this.reservaService = reservaService;
        this.usuarioRepo = usuarioRepo;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@RequestParam Long tipoHabitacionId,
                         @RequestParam LocalDate fechaEntrada,
                         @RequestParam LocalDate fechaSalida,
                         Authentication auth) {
        Long usuarioId = obtenerUsuarioId(auth);
        return reservaService.crearReserva(usuarioId, tipoHabitacionId, fechaEntrada, fechaSalida);
    }
    @GetMapping("/mis-reservas")
    public List<Reserva> misReservas(Authentication auth) {
        Long usuarioId = obtenerUsuarioId(auth);
        return reservaService.misReservas(usuarioId);
    }
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('JEFE', 'RECEPCION')")
    public List<Reserva> pendientes() {
        return reservaService.reservasPendientes();
    }
    @GetMapping("/{id}")
    public Reserva obtener(@PathVariable Long id) {
        return reservaService.obtenerReserva(id);
    }
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('JEFE', 'RECEPCION')")
    public Reserva checkIn(@PathVariable Long id) {
        return reservaService.checkIn(id);
    }
    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('JEFE', 'RECEPCION')")
    public Reserva checkOut(@PathVariable Long id) {
        return reservaService.checkOut(id);
    }
    private Long obtenerUsuarioId(Authentication auth) {
        String username = (String) auth.getPrincipal();
        return usuarioRepo.findByUsername(username)
            .orElseThrow()
            .getId();
    }
}