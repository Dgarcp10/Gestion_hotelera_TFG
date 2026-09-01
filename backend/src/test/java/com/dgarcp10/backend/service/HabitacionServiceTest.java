package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.TareaLimpiezaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HabitacionServiceTest {
    @Autowired HabitacionService habitacionService;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired TareaLimpiezaRepository tareaRepo;
    private TipoHabitacion nuevoTipo(String nombre) {
        TipoHabitacion t = new TipoHabitacion();
        t.setNombre(nombre);
        t.setCapacidad(2);
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
    void listarTodos_derivaPendienteDeProximaLimpieza() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion h = nuevaHabitacion(301, tipo, EstadoHabitacion.LIBRE);
        h.setProximaLimpieza(LocalDate.now());
        habitacionRepo.save(h);
        List<Habitacion> todas = habitacionService.listarTodos();
        Habitacion resultado = todas.stream().filter(x -> x.getId().equals(h.getId()))
            .findFirst().orElseThrow();
        assertTrue(resultado.getPendienteLimpieza());
    }
    @Test
    void crear_numeroDuplicado_lanza() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion h = nuevaHabitacion(302, tipo, EstadoHabitacion.LIBRE);
        Habitacion duplicada = new Habitacion();
        duplicada.setNumero(h.getNumero());
        duplicada.setTipoHabitacion(tipo);
        assertThrows(IllegalArgumentException.class, () -> habitacionService.crear(duplicada));
    }
    @Test
    void crear_sinTipo_lanza() {
        Habitacion h = new Habitacion();
        h.setNumero(303);
        assertThrows(IllegalArgumentException.class, () -> habitacionService.crear(h));
    }
    @Test
    void obtener_noExiste_lanza() {
        assertThrows(NoSuchElementException.class, () -> habitacionService.obtenerPorId(99999L));
    }
    @Test
    void actualizar_marcaPendiente_creaTareaDeLimpieza() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion h = nuevaHabitacion(304, tipo, EstadoHabitacion.LIBRE);
        Habitacion datos = new Habitacion();
        datos.setPendienteLimpieza(true);
        habitacionService.actualizar(h.getId(), datos, 1L);
        assertTrue(tareaRepo.findByHabitacionIdAndCompletadaEnIsNull(h.getId()).isPresent());
    }
    @Test
    void eliminar_borraHabitacion() {
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion h = nuevaHabitacion(305, tipo, EstadoHabitacion.LIBRE);
        habitacionService.eliminar(h.getId());
        assertThrows(NoSuchElementException.class, () -> habitacionService.obtenerPorId(h.getId()));
    }
}