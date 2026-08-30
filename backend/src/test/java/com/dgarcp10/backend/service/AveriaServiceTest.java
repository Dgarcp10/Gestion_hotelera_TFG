package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.Averia;
import com.dgarcp10.backend.model.EstadoAveria;
import com.dgarcp10.backend.model.EstadoBloqueo;
import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.GravedadAveria;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.BloqueoHabitacionRepository;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AveriaServiceTest {
    @Autowired AveriaService averiaService;
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired BloqueoHabitacionRepository bloqueoRepo;
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
    private TipoHabitacion nuevoTipo(String nombre, int capacidad) {
        TipoHabitacion t = new TipoHabitacion();
        t.setNombre(nombre);
        t.setCapacidad(capacidad);
        t.setPrecioBase(new BigDecimal("100.00"));
        return tipoRepo.save(t);
    }
    private Habitacion nuevaHabitacion(int numero, TipoHabitacion tipo, EstadoHabitacion estado) {
        Habitacion h = new Habitacion();
        h.setNumero(numero);
        h.setTipoHabitacion(tipo);
        h.setEstado(estado);
        h.setPendienteLimpieza(false);
        h.setAveriada(false);
        return habitacionRepo.save(h);
    }
    @Test
    void crearLeve_marcaAveriadaSinBloquear() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(101, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.LIMPIEZA);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.LEVE, "Grifo que gotea", trab.getId());
        assertEquals(EstadoAveria.ABIERTA, a.getEstado());
        assertTrue(hab.getAveriada());
        assertEquals(EstadoHabitacion.LIBRE, hab.getEstado());
        assertNull(a.getBloqueo());
        assertTrue(bloqueoRepo.findAll().isEmpty());
    }
    @Test
    void crearGrave_bloqueaHabitacion() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(102, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.GRAVE, "AA roto", trab.getId());
        assertNotNull(a.getBloqueo());
        assertEquals(EstadoBloqueo.ACTIVO, a.getBloqueo().getEstado());
        assertEquals(EstadoHabitacion.BLOQUEADA, hab.getEstado());
        assertTrue(hab.getAveriada());
    }
    @Test
    void crear_duplicadaAbierta_lanza() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(103, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.RECEPCION);
        averiaService.crear(hab.getId(), GravedadAveria.LEVE, "Primera", trab.getId());
        assertThrows(IllegalStateException.class,
            () -> averiaService.crear(hab.getId(), GravedadAveria.GRAVE, "Segunda", trab.getId()));
    }
    @Test
    void resolverGrave_liberaHabitacionYlevantaBloqueo() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(104, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.GRAVE, "Fontanería", trab.getId());
        Averia resuelta = averiaService.resolver(a.getId());
        assertEquals(EstadoAveria.RESUELTA, resuelta.getEstado());
        assertFalse(hab.getAveriada());
        assertEquals(EstadoHabitacion.LIBRE, hab.getEstado());
        assertEquals(EstadoBloqueo.CANCELADO, resuelta.getBloqueo().getEstado());
    }
    @Test
    void resolver_yaResuelta_lanza() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(105, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.JEFE);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.LEVE, "Luz", trab.getId());
        averiaService.resolver(a.getId());
        assertThrows(IllegalStateException.class, () -> averiaService.resolver(a.getId()));
    }
    @Test
    void actualizar_leveAGrave_creaBloqueoYbloquea() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(106, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.LEVE, "Grifo", trab.getId());
        Averia g = averiaService.actualizar(a.getId(), GravedadAveria.GRAVE, null,
            LocalDate.now(), LocalDate.now().plusDays(1), false, trab.getId());
        assertNotNull(g.getBloqueo());
        assertEquals(EstadoBloqueo.ACTIVO, g.getBloqueo().getEstado());
        assertEquals(EstadoHabitacion.BLOQUEADA, hab.getEstado());
    }
    @Test
    void actualizar_graveCambiaFechas_actualizaBloqueo() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(107, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.RECEPCION);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.GRAVE, "AA",
            trab.getId(), LocalDate.now(), LocalDate.now().plusDays(1), false);
        Averia g = averiaService.actualizar(a.getId(), GravedadAveria.GRAVE, "repaso",
            LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), false, trab.getId());
        assertEquals(LocalDate.now().plusDays(2), g.getBloqueo().getFechaInicio());
        assertEquals(LocalDate.now().plusDays(5), g.getBloqueo().getFechaFin());
    }
    @Test
    void actualizar_graveALeve_cancelaBloqueo() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(108, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.JEFE);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.GRAVE, "AA",
            trab.getId(), LocalDate.now(), LocalDate.now().plusDays(1), false);
        Averia l = averiaService.actualizar(a.getId(), GravedadAveria.LEVE, null, null, null, false, trab.getId());
        assertEquals(GravedadAveria.LEVE, l.getGravedad());
        assertEquals(EstadoBloqueo.CANCELADO, l.getBloqueo().getEstado());
        assertEquals(EstadoHabitacion.LIBRE, hab.getEstado());
    }
    @Test
    void actualizar_upgradeGraveSinFechas_lanza() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(109, tipo, EstadoHabitacion.LIBRE);
        Usuario trab = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Averia a = averiaService.crear(hab.getId(), GravedadAveria.LEVE, "Luz", trab.getId());
        assertThrows(IllegalArgumentException.class,
            () -> averiaService.actualizar(a.getId(), GravedadAveria.GRAVE, null,
                null, null, false, trab.getId()));
    }
}