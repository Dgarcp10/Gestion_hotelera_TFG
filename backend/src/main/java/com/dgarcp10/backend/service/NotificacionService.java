package com.dgarcp10.backend.service;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.BloqueoHabitacion;
import com.dgarcp10.backend.model.Notificacion;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.NotificacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepo;
    private final UsuarioRepository usuarioRepo;
    public NotificacionService(NotificacionRepository notificacionRepo,
                               UsuarioRepository usuarioRepo) {
        this.notificacionRepo = notificacionRepo;
        this.usuarioRepo = usuarioRepo;
    }
    public void notificarA(Usuario usuario, String tipo, String mensaje, BloqueoHabitacion bloqueo) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setTipo(tipo);
        n.setMensaje(mensaje);
        n.setBloqueo(bloqueo);
        n.setCreadoEn(Instant.now());
        notificacionRepo.save(n);
    }
    public void notificarRoles(List<RolUsuario> roles, String tipo, String mensaje,
                               BloqueoHabitacion bloqueo) {
        for (Usuario usuario : usuarioRepo.findByRolIn(roles)) {
            notificarA(usuario, tipo, mensaje, bloqueo);
        }
    }
    public List<Notificacion> misNotificaciones(Long usuarioId) {
        return notificacionRepo.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
    }
    public long countNoLeidas(Long usuarioId) {
        return notificacionRepo.countByUsuarioIdAndLeidaFalse(usuarioId);
    }
    @Transactional
    public void marcarLeida(Long id, Long usuarioId) {
        Notificacion n = notificacionRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Notificación no encontrada: " + id));
        if (!n.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No puedes marcar como leída una notificación ajena");
        }
        n.setLeida(true);
        notificacionRepo.save(n);
    }
}