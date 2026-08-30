package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.BloqueoHabitacion;
import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.NotificacionRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BloqueoServiceTest {
    @Autowired BloqueoService bloqueoService;
    @Autowired ReservaService reservaService;
    @Autowired NotificacionRepository notificacionRepo;
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired ReservaRepository reservaRepo;
    private static final LocalDate HOY = LocalDate.now();
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
    private Habitacion nuevaHabitacion(int numero, TipoHabitacion tipo, EstadoHabitacion estado,
                                       boolean pendienteLimpieza) {
        Habitacion h = new Habitacion();
        h.setNumero(numero);
        h.setTipoHabitacion(tipo);
        h.setEstado(estado);
        h.setPendienteLimpieza(pendienteLimpieza);
        h.setAveriada(false);
        return habitacionRepo.save(h);
    }
    private Reserva nuevaEstancia(Usuario huesped, TipoHabitacion tipo, Habitacion hab,
                                  LocalDate entrada, LocalDate salida, EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setUsuario(huesped);
        r.setTipoHabitacion(tipo);
        r.setFechaEntrada(entrada);
        r.setFechaSalida(salida);
        r.setPrecioTotal(new BigDecimal("400.00"));
        r.setImporteCobrado(new BigDecimal("400.00"));
        r.setEstado(estado);
        r.setCreadoEn(Instant.now());
        r.setHabitacion(hab);
        return reservaRepo.save(r);
    }
    @Test
    void crear_solapamientoActivo_lanza() {
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(201, tipo, EstadoHabitacion.LIBRE, false);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        bloqueoService.crear(hab.getId(), HOY, HOY.plusDays(2), "Obras", false, jefe);
        assertThrows(IllegalStateException.class,
            () -> bloqueoService.crear(hab.getId(), HOY.plusDays(1), HOY.plusDays(3), "Pintura", false, jefe));
    }
    @Test
    void conHuesped_reubicaPreferendoMismoTipo() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(202, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion x2 = nuevaHabitacion(203, doble, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario mant = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(2), HOY.plusDays(4), EstadoReserva.EN_CURSO);
        bloqueoService.crear(x.getId(), HOY, HOY.plusDays(1), "Avería grave", false, mant);
        assertEquals(x2.getId(), r.getHabitacion().getId());
        assertEquals(doble.getId(), r.getTipoHabitacion().getId());
        assertEquals(EstadoHabitacion.OCUPADA, x2.getEstado());
        assertEquals(EstadoHabitacion.BLOQUEADA, x.getEstado());
    }
    @Test
    void tipoDobleLlenoConLlegadaFutura_reubicaATipoSuperior() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        TipoHabitacion triple = nuevoTipo("Triple", 3);
        Habitacion x = nuevaHabitacion(204, doble, EstadoHabitacion.OCUPADA, false);
        nuevaHabitacion(205, doble, EstadoHabitacion.LIBRE, false);
        Habitacion y = nuevaHabitacion(206, triple, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(5), EstadoReserva.EN_CURSO);
        nuevaEstancia(nuevoUsuario(RolUsuario.USUARIO), doble, null,
            HOY.plusDays(2), HOY.plusDays(4), EstadoReserva.PENDIENTE);
        bloqueoService.crear(x.getId(), HOY, HOY.plusDays(4), "Inundación", false, jefe);
        assertEquals(y.getId(), r.getHabitacion().getId());
        assertEquals(triple.getId(), r.getTipoHabitacion().getId());
    }
    @Test
    void sinHueco_requiereConfirmacion_yConElla_aplicaYnotifica() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(207, doble, EstadoHabitacion.OCUPADA, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(3), EstadoReserva.EN_CURSO);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> bloqueoService.crear(x.getId(), HOY, HOY.plusDays(2), "Obras", false, jefe));
        assertTrue(ex.getMessage().contains("Confirma"));
        assertEquals(0, notificacionRepo.findAll().size());
        BloqueoHabitacion b = bloqueoService.crear(x.getId(), HOY, HOY.plusDays(2), "Obras", true, jefe);
        assertTrue(b.getConfirmadoSinReubicacion());
        assertEquals(EstadoHabitacion.BLOQUEADA, x.getEstado());
        assertEquals(x.getId(), r.getHabitacion().getId());
        assertTrue(notificacionRepo.findAll().stream()
            .anyMatch(n -> n.getMensaje().contains("debe realizarse una reubicación manual")));
    }
    @Test
    void reubicacion_prefiereHabitacionLimpia() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        TipoHabitacion triple = nuevoTipo("Triple", 3);
        Habitacion x = nuevaHabitacion(208, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion limpia = nuevaHabitacion(209, triple, EstadoHabitacion.LIBRE, false);
        nuevaHabitacion(210, triple, EstadoHabitacion.LIBRE, true);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(3), EstadoReserva.EN_CURSO);
        bloqueoService.crear(x.getId(), HOY, HOY.plusDays(1), "Avería grave", false, jefe);
        assertEquals(limpia.getId(), r.getHabitacion().getId());
    }
    @Test
    void soloSuciaDisponible_laEligeYNotificaALimpieza() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        TipoHabitacion triple = nuevoTipo("Triple", 3);
        Habitacion x = nuevaHabitacion(211, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion sucia = nuevaHabitacion(212, triple, EstadoHabitacion.LIBRE, true);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario limpieza = nuevoUsuario(RolUsuario.LIMPIEZA);
        Usuario mant = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(3), EstadoReserva.EN_CURSO);
        bloqueoService.crear(x.getId(), HOY, HOY.plusDays(1), "Avería grave", false, mant);
        assertEquals(sucia.getId(), r.getHabitacion().getId());
        assertTrue(notificacionRepo.findByUsuarioIdOrderByCreadoEnDesc(limpieza.getId()).stream()
            .anyMatch(n -> n.getMensaje().contains("limpieza pendiente")));
    }
    @Test
    void crearReserva_bloqueoAgotaDisponibilidad_rechaza() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion hab = nuevaHabitacion(213, doble, EstadoHabitacion.LIBRE, false);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        reservaService.crearReserva(nuevoUsuario(RolUsuario.USUARIO).getId(),
            doble.getId(), HOY.plusDays(1), HOY.plusDays(3), "1234567890123456", "12/30", "123");
        bloqueoService.crear(hab.getId(), HOY.plusDays(1), HOY.plusDays(3), "Obras", false, jefe);
        assertThrows(IllegalStateException.class, () -> reservaService.crearReserva(
            nuevoUsuario(RolUsuario.USUARIO).getId(), doble.getId(), HOY.plusDays(1), HOY.plusDays(3),
            "1234567890123456", "12/30", "123"));
    }
    @Test
    void huespedEntraHoy_averiaGravePorDefecto_reubicaYbloquea() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(220, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion x2 = nuevaHabitacion(221, doble, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario mant = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY, HOY.plusDays(4), EstadoReserva.EN_CURSO);
        bloqueoService.bloquearPorAveria(x, mant, null, null, false);
        assertEquals(x2.getId(), r.getHabitacion().getId());
        assertEquals(EstadoHabitacion.BLOQUEADA, x.getEstado());
        assertEquals(EstadoHabitacion.OCUPADA, x2.getEstado());
    }
    @Test
    void rangoMismoDia_entradaHoy_noSeLeEscapa() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(222, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion x2 = nuevaHabitacion(223, doble, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario mant = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY, HOY.plusDays(4), EstadoReserva.EN_CURSO);
        bloqueoService.crear(x.getId(), HOY, HOY, "Avería grave", false, mant);
        assertEquals(x2.getId(), r.getHabitacion().getId());
        assertEquals(EstadoHabitacion.BLOQUEADA, x.getEstado());
    }
    @Test
    void averiaGrave_fechasExplicitas_reubica() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(224, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion x2 = nuevaHabitacion(225, doble, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario mant = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(5), EstadoReserva.EN_CURSO);
        bloqueoService.bloquearPorAveria(x, mant, HOY, HOY.plusDays(3), false);
        assertEquals(x2.getId(), r.getHabitacion().getId());
        assertEquals(EstadoHabitacion.BLOQUEADA, x.getEstado());
    }
    @Test
    void reubicacion_notificaARolesEIncluyeAutorConDetalle() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(226, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion x2 = nuevaHabitacion(227, doble, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Usuario mant = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(3), EstadoReserva.EN_CURSO);
        bloqueoService.crear(x.getId(), HOY, HOY.plusDays(1), "Avería grave", false, mant);
        String origenDestino = "de la habitación " + x.getNumero() + " a la habitación " + x2.getNumero();
        assertTrue(notificacionRepo.findByUsuarioIdOrderByCreadoEnDesc(jefe.getId()).stream()
            .anyMatch(n -> n.getMensaje().contains(origenDestino)));
        assertTrue(notificacionRepo.findByUsuarioIdOrderByCreadoEnDesc(mant.getId()).stream()
            .anyMatch(n -> n.getMensaje().contains(origenDestino)));
        assertTrue(notificacionRepo.findByUsuarioIdOrderByCreadoEnDesc(jefe.getId()).stream()
            .anyMatch(n -> n.getMensaje().contains(huesped.getNombre())));
    }
    @Test
    void preview_sinHuesped_reportaVacio() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(228, doble, EstadoHabitacion.LIBRE, false);
        ReubicacionPreview p = bloqueoService.previewReubicacion(x.getId(), HOY, HOY.plusDays(2));
        assertFalse(p.hayHuesped());
        assertFalse(p.yaBloqueada());
        assertNull(p.habitacionDestino());
    }
    @Test
    void preview_huesped_destinoCategoriaSuperior_noMuta() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        TipoHabitacion triple = nuevoTipo("Triple", 3);
        Habitacion x = nuevaHabitacion(229, doble, EstadoHabitacion.OCUPADA, false);
        Habitacion y = nuevaHabitacion(231, triple, EstadoHabitacion.LIBRE, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        Reserva r = nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(5), EstadoReserva.EN_CURSO);
        ReubicacionPreview p = bloqueoService.previewReubicacion(x.getId(), HOY, HOY.plusDays(2));
        assertTrue(p.hayHuesped());
        assertTrue(p.categoriaSuperior());
        assertTrue(p.hayHueco());
        assertEquals(Integer.valueOf(y.getNumero()), p.habitacionDestino());
        assertEquals(x.getId(), r.getHabitacion().getId());
        assertEquals(EstadoHabitacion.OCUPADA, x.getEstado());
        assertEquals(EstadoHabitacion.LIBRE, y.getEstado());
    }
    @Test
    void preview_sinHueco_reportaHayHuecoFalse() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(232, doble, EstadoHabitacion.OCUPADA, false);
        nuevaHabitacion(233, doble, EstadoHabitacion.OCUPADA, false);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        nuevaEstancia(huesped, doble, x, HOY.minusDays(1), HOY.plusDays(5), EstadoReserva.EN_CURSO);
        ReubicacionPreview p = bloqueoService.previewReubicacion(x.getId(), HOY, HOY.plusDays(2));
        assertTrue(p.hayHuesped());
        assertFalse(p.hayHueco());
    }
    @Test
    void preview_yaBloqueada_reportaFlag() {
        TipoHabitacion doble = nuevoTipo("Doble", 2);
        Habitacion x = nuevaHabitacion(234, doble, EstadoHabitacion.LIBRE, false);
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        bloqueoService.crear(x.getId(), HOY, HOY.plusDays(2), "Obras", false, jefe);
        ReubicacionPreview p = bloqueoService.previewReubicacion(x.getId(), HOY.plusDays(1), HOY.plusDays(2));
        assertTrue(p.yaBloqueada());
    }
}