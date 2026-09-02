package com.dgarcp10.backend.service;
import java.time.Instant;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceTest {
    @Autowired UsuarioService usuarioService;
    @Autowired BCryptPasswordEncoder encoder;
    private Usuario nuevoUsuario(RolUsuario rol) {
        Usuario u = new Usuario();
        u.setUsername("u" + System.nanoTime());
        u.setEmail(u.getUsername() + "@test.es");
        u.setPasswordHash("pass123");
        u.setNombre("Test");
        u.setApellido("Test");
        u.setRol(rol);
        u.setCreadoEn(Instant.now());
        return u;
    }
    @Test
    void crear_sinRol_asignaUsuarioPorDefecto() {
        Usuario guardado = usuarioService.crear(nuevoUsuario(null));
        assertEquals(RolUsuario.USUARIO, guardado.getRol());
    }
    @Test
    void crear_codificaPasswordConBCrypt() {
        Usuario guardado = usuarioService.crear(nuevoUsuario(RolUsuario.USUARIO));
        assertNotEquals("pass123", guardado.getPasswordHash());
        assertTrue(encoder.matches("pass123", guardado.getPasswordHash()));
    }
    @Test
    void crear_usernameDuplicado_lanza() {
        Usuario a = usuarioService.crear(nuevoUsuario(RolUsuario.USUARIO));
        Usuario b = nuevoUsuario(RolUsuario.USUARIO);
        b.setUsername(a.getUsername());
        assertThrows(DataIntegrityViolationException.class, () -> usuarioService.crear(b));
    }
    @Test
    void obtenerPorId_noExiste_lanza() {
        assertThrows(NoSuchElementException.class, () -> usuarioService.obtenerPorId(99999L));
    }
    @Test
    void obtenerPorUsername_descubreUsuario() {
        Usuario u = usuarioService.crear(nuevoUsuario(RolUsuario.LIMPIEZA));
        assertEquals(u.getId(), usuarioService.obtenerPorUsername(u.getUsername()).getId());
    }
    @Test
    void actualizar_cambiaDatosYcodificaNuevaPassword() {
        Usuario u = usuarioService.crear(nuevoUsuario(RolUsuario.USUARIO));
        Usuario datos = nuevoUsuario(RolUsuario.JEFE);
        datos.setNombre("Nuevo");
        datos.setPasswordHash("nuevaPass");
        Usuario actualizado = usuarioService.actualizar(u.getId(), datos);
        assertEquals("Nuevo", actualizado.getNombre());
        assertEquals(RolUsuario.JEFE, actualizado.getRol());
        assertTrue(encoder.matches("nuevaPass", actualizado.getPasswordHash()));
    }
    @Test
    void eliminar_noExiste_lanza() {
        assertThrows(NoSuchElementException.class, () -> usuarioService.eliminar(99999L));
    }
}