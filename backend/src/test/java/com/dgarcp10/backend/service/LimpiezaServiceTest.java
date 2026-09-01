package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TareaLimpieza;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.TareaLimpiezaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LimpiezaServiceTest {
    @Autowired LimpiezaService limpiezaService;
    @Autowired TareaLimpiezaRepository tareaRepo;
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired UsuarioRepository usuarioRepo;
    private TipoHabitacion nuevoTipo(String nombre) {
        TipoHabitacion t = new TipoHabitacion();
        t.setNombre(nombre);
        t.setCapacidad(2);
        t.setPrecioBase(new BigDecimal("100.00"));
        return tipoRepo.save(t);
    }
    private Habitacion nuevaHabitacion(int numero, TipoHabitacion tipo) {
        Habitacion h = new Habitacion();
        h.setNumero(numero);
        h.setTipoHabitacion(tipo);
        h.setEstado(EstadoHabitacion.LIBRE);
        h.setPendienteLimpieza(false);
        h.setAveriada(false);
        return habitacionRepo.save(h);
    }
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
    void programarLimpieza_creaTareaYpendiente() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion hab = nuevaHabitacion(401, tipo);
        limpiezaService.programarLimpieza(hab.getNumero());
        assertTrue(tareaRepo.findByHabitacionIdAndCompletadaEnIsNull(hab.getId()).isPresent());
        assertTrue(habitacionRepo.findById(hab.getId()).orElseThrow().getPendienteLimpieza());
    }
    @Test
    void programar_yaTieneTareaPendiente_lanza() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion hab = nuevaHabitacion(402, tipo);
        limpiezaService.programarLimpieza(hab.getNumero());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> limpiezaService.programarLimpieza(hab.getNumero()));
        assertTrue(ex.getMessage().contains("ya tiene una tarea"));
    }
    @Test
    void completarTarea_dejaLimpiaYregistraRecurso() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion hab = nuevaHabitacion(403, tipo);
        limpiezaService.programarLimpieza(hab.getNumero());
        TareaLimpieza tarea = tareaRepo.findByHabitacionIdAndCompletadaEnIsNull(hab.getId()).orElseThrow();
        Usuario limpieza = nuevoUsuario(RolUsuario.LIMPIEZA);
        limpiezaService.completarTarea(tarea.getId(), limpieza.getId());
        assertFalse(habitacionRepo.findById(hab.getId()).orElseThrow().getPendienteLimpieza());
        TareaLimpieza completada = tareaRepo.findById(tarea.getId()).orElseThrow();
        assertNotNull(completada.getCompletadaEn());
        assertEquals(limpieza.getId(), completada.getCompletadoPor().getId());
    }
    @Test
    void completarTarea_noExiste_lanza() {
        assertThrows(NoSuchElementException.class, () -> limpiezaService.completarTarea(99999L, 1L));
    }
    @Test
    void habitacionesParaLimpiar_devuelveSoloLasPendientes() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        nuevaHabitacion(404, tipo);
        Habitacion pendiente = nuevaHabitacion(405, tipo);
        pendiente.setPendienteLimpieza(true);
        pendiente.setProximaLimpieza(LocalDate.now());
        habitacionRepo.save(pendiente);
        assertEquals(1, limpiezaService.habitacionesParaLimpiar().stream()
            .filter(h -> h.getNumero().equals(405)).count());
    }
}