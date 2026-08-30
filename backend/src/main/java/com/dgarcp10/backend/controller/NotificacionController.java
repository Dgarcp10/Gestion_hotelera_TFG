package com.dgarcp10.backend.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dgarcp10.backend.model.Notificacion;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;
import com.dgarcp10.backend.service.NotificacionService;
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepo;
    public NotificacionController(NotificacionService notificacionService, UsuarioRepository usuarioRepo) {
        this.notificacionService = notificacionService;
        this.usuarioRepo = usuarioRepo;
    }
    @GetMapping
    public List<Notificacion> misNotificaciones(Authentication auth) {
        return notificacionService.misNotificaciones(obtenerUsuarioId(auth));
    }
    @GetMapping("/no-leidas")
    public long countNoLeidas(Authentication auth) {
        return notificacionService.countNoLeidas(obtenerUsuarioId(auth));
    }
    @PostMapping("/{id}/leida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeida(@PathVariable Long id, Authentication auth) {
        Usuario usuario = usuarioRepo.findByUsername((String) auth.getPrincipal()).orElseThrow();
        if (usuario.getRol() != RolUsuario.JEFE && usuario.getRol() != RolUsuario.RECEPCION) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Solo el jefe o recepción puede marcar notificaciones como leídas");
        }
        notificacionService.marcarLeida(id, usuario.getId());
    }
    private Long obtenerUsuarioId(Authentication auth) {
        return usuarioRepo.findByUsername((String) auth.getPrincipal()).orElseThrow().getId();
    }
}