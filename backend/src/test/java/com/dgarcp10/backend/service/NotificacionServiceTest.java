package com.dgarcp10.backend.service;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.Notificacion;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.NotificacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificacionServiceTest {
    @Autowired NotificacionService notificacionService;
    @Autowired NotificacionRepository notificacionRepo;
    @Autowired UsuarioRepository usuarioRepo;
    private Usuario nuevoUsuario(RolUsuario rol) {
        Usuario u = new Usuario();
        u.setUsername("u" + System.nanoTime());
        u.setEmail(u.getUsername() + "@test.es");
        u.setPasswordHash("x");
        u.setNombre("Test");
        u.setApellido("Test");
        u.setRol(rol);
        u.setCreadoEn(Instant.now());
        return usuarioRepo.save(u);
    }
    @Test
    void notificarRoles_notificaATodosLosDelRol() {
        Usuario j1 = nuevoUsuario(RolUsuario.JEFE);
        Usuario j2 = nuevoUsuario(RolUsuario.JEFE);
        notificacionService.notificarRoles(List.of(RolUsuario.JEFE), "INFO", "Mensaje", null);
        assertEquals(1, notificacionService.misNotificaciones(j1.getId()).size());
        assertEquals(1, notificacionService.misNotificaciones(j2.getId()).size());
    }
    @Test
    void misNotificaciones_soloLasPropias() {
        Usuario a = nuevoUsuario(RolUsuario.USUARIO);
        Usuario b = nuevoUsuario(RolUsuario.USUARIO);
        notificacionService.notificarA(a, "INFO", "Para A", null);
        notificacionService.notificarA(b, "INFO", "Para B", null);
        List<Notificacion> deA = notificacionService.misNotificaciones(a.getId());
        assertEquals(1, deA.size());
        assertTrue(deA.get(0).getMensaje().contains("Para A"));
    }
    @Test
    void marcarLeida_propia_marcaTrue() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        notificacionService.notificarA(u, "INFO", "Hola", null);
        Notificacion n = notificacionService.misNotificaciones(u.getId()).get(0);
        notificacionService.marcarLeida(n.getId(), u.getId());
        assertTrue(notificacionRepo.findById(n.getId()).orElseThrow().getLeida());
        assertEquals(0, notificacionService.countNoLeidas(u.getId()));
    }
    @Test
    void marcarLeida_ajena_lanza() {
        Usuario a = nuevoUsuario(RolUsuario.USUARIO);
        Usuario b = nuevoUsuario(RolUsuario.USUARIO);
        notificacionService.notificarA(a, "INFO", "Para A", null);
        Notificacion n = notificacionService.misNotificaciones(a.getId()).get(0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> notificacionService.marcarLeida(n.getId(), b.getId()));
        assertTrue(ex.getMessage().contains("notificación ajena"));
    }
    @Test
    void marcarLeida_noExiste_lanza() {
        assertThrows(NoSuchElementException.class,
            () -> notificacionService.marcarLeida(99999L, 1L));
    }
}