package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.Averia;
import com.dgarcp10.backend.model.BloqueoHabitacion;
import com.dgarcp10.backend.model.EstadoBloqueo;
import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoPago;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.GravedadAveria;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Notificacion;
import com.dgarcp10.backend.model.Pago;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TareaLimpieza;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.TipoTareaLimpieza;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.BloqueoHabitacionRepository;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.NotificacionRepository;
import com.dgarcp10.backend.repository.PagoRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TareaLimpiezaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardServiceTest {
    @Autowired DashboardService dashboardService;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired ReservaRepository reservaRepo;
    @Autowired PagoRepository pagoRepo;
    @Autowired TareaLimpiezaRepository tareaRepo;
    @Autowired BloqueoHabitacionRepository bloqueoRepo;
    @Autowired NotificacionRepository notificacionRepo;
    @Autowired AveriaService averiaService;
    private static final int ANIO = 2030;
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
    private Habitacion nuevaHabitacion(int numero, TipoHabitacion tipo) {
        Habitacion h = new Habitacion();
        h.setNumero(numero);
        h.setTipoHabitacion(tipo);
        h.setEstado(EstadoHabitacion.LIBRE);
        h.setPendienteLimpieza(false);
        h.setAveriada(false);
        return habitacionRepo.save(h);
    }
    private Reserva nuevaReserva(Usuario u, TipoHabitacion tipo, LocalDate entrada, LocalDate salida,
                                 BigDecimal total, boolean cancelada) {
        Reserva r = new Reserva();
        r.setUsuario(u);
        r.setTipoHabitacion(tipo);
        r.setFechaEntrada(entrada);
        r.setFechaSalida(salida);
        r.setPrecioTotal(total);
        r.setImporteCobrado(total);
        r.setEstado(cancelada ? EstadoReserva.CANCELADA : EstadoReserva.PENDIENTE);
        r.setCreadoEn(Instant.now());
        r = reservaRepo.save(r);
        Pago p = new Pago();
        p.setReserva(r);
        p.setImporte(total);
        p.setEstado(cancelada ? EstadoPago.REEMBOLSADO : EstadoPago.COMPLETADO);
        p.setReferencia("PAY-DASH");
        p.setCreadoEn(Instant.now());
        r.setPago(pagoRepo.save(p));
        return reservaRepo.save(r);
    }
    private Notificacion notificacionConBloqueo(Usuario creadoPor, Habitacion hab) {
        BloqueoHabitacion b = new BloqueoHabitacion();
        b.setHabitacion(hab);
        b.setFechaInicio(LocalDate.now());
        b.setFechaFin(LocalDate.now().plusDays(1));
        b.setMotivo("Test bloqueo");
        b.setCreadoPor(creadoPor);
        b.setEstado(EstadoBloqueo.ACTIVO);
        b.setConfirmadoSinReubicacion(false);
        b.setCreadoEn(Instant.now());
        b = bloqueoRepo.save(b);
        Notificacion n = new Notificacion();
        n.setUsuario(creadoPor);
        n.setTipo("BLOQUEO");
        n.setMensaje("Habitación bloqueada por avería");
        n.setBloqueo(b);
        n.setLeida(false);
        n.setCreadoEn(Instant.now());
        return notificacionRepo.save(n);
    }
    private Notificacion notificacionSinBloqueo(Usuario u) {
        Notificacion n = new Notificacion();
        n.setUsuario(u);
        n.setTipo("AVISO");
        n.setMensaje("Tienes reservas para hoy");
        n.setBloqueo(null);
        n.setLeida(false);
        n.setCreadoEn(Instant.now());
        return notificacionRepo.save(n);
    }
    @Test
    void graficos_ocupacionYGanancias_prorrateadas() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(101, tipo);
        nuevaReserva(u, tipo, LocalDate.of(ANIO, 3, 1), LocalDate.of(ANIO, 3, 31),
            new BigDecimal("300.00"), false);
        DashboardGraficos g = dashboardService.resumenGraficos(ANIO, 3);
        assertEquals(100.0, g.ocupacionMensual().get(0).porcentaje(), 0.0001);
        assertEquals(0.0, g.ocupacionMensual().get(30).porcentaje(), 0.0001);
        assertEquals(96.8, g.ocupacionAnual().get(2).porcentaje(), 0.0001);
        assertEquals(0.0, g.ocupacionAnual().get(0).porcentaje(), 0.0001);
        assertEquals(new BigDecimal("10.00"), g.gananciasMensuales().get(1).importe());
        assertEquals(new BigDecimal("300.00"), g.gananciasAnuales().get(2).importe());
        assertEquals(new BigDecimal("0.00"), g.gananciasAnuales().get(0).importe());
    }
    @Test
    void graficos_canceladasNoAportanOcupacionNIGanancias() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(201, tipo);
        nuevaReserva(u, tipo, LocalDate.of(ANIO, 3, 1), LocalDate.of(ANIO, 3, 20),
            new BigDecimal("5000.00"), true);
        DashboardGraficos g = dashboardService.resumenGraficos(ANIO, 3);
        assertEquals(0.0, g.ocupacionAnual().get(2).porcentaje(), 0.0001);
        assertEquals(0.0, g.ocupacionMensual().get(4).porcentaje(), 0.0001);
        assertEquals(new BigDecimal("0.00"), g.gananciasAnuales().get(2).importe());
        assertEquals(new BigDecimal("0.00"), g.gananciasMensuales().get(4).importe());
    }
    @Test
    void graficos_mesInvalido_lanza() {
        assertThrows(IllegalArgumentException.class, () -> dashboardService.resumenGraficos(ANIO, 13));
    }
    @Test
    void seguimiento_rangoParticionaNotificacionesYTrazaResolucion() {
        Usuario trab = nuevoUsuario(RolUsuario.MANTENIMIENTO);
        TipoHabitacion tipoDoble = nuevoTipo("Doble", 2);
        TipoHabitacion tipoInd = nuevoTipo("Individual", 1);
        Habitacion h1 = nuevaHabitacion(101, tipoDoble);
        nuevaHabitacion(102, tipoInd);
        TareaLimpieza t = new TareaLimpieza();
        t.setHabitacion(h1);
        t.setTipo(TipoTareaLimpieza.CHECKOUT);
        t.setAccionable(true);
        t.setCompletadoPor(trab);
        t.setCompletadaEn(Instant.now());
        t.setCreadoEn(Instant.now().minusSeconds(600));
        tareaRepo.save(t);
        Averia averia = averiaService.crear(h1.getId(), GravedadAveria.LEVE, "Grifo", trab.getId());
        averiaService.resolver(averia.getId(), trab.getId());
        notificacionConBloqueo(trab, h1);
        notificacionSinBloqueo(trab);
        LocalDate hoy = LocalDate.now();
        DashboardSeguimiento todo = dashboardService.seguimiento(hoy, hoy, null, null);
        assertEquals(1, todo.tareasLimpieza().size());
        assertEquals(1, todo.averias().size());
        assertEquals(1, todo.notificacionesConHabitacion().size());
        assertEquals(1, todo.notificacionesSinHabitacion().size());
        assertTrue(todo.averias().get(0).resueltaPor().contains("Test"));
        assertEquals("Doble", todo.tareasLimpieza().get(0).habitacionTipo());
        DashboardSeguimiento porNumero = dashboardService.seguimiento(hoy, hoy, null, 101);
        assertEquals(1, porNumero.tareasLimpieza().size());
        assertEquals(1, porNumero.averias().size());
        assertEquals(1, porNumero.notificacionesConHabitacion().size());
        assertTrue(porNumero.notificacionesSinHabitacion().isEmpty());
        DashboardSeguimiento porTipo = dashboardService.seguimiento(hoy, hoy, tipoInd.getId(), null);
        assertTrue(porTipo.tareasLimpieza().isEmpty());
        assertTrue(porTipo.averias().isEmpty());
        assertTrue(porTipo.notificacionesConHabitacion().isEmpty());
        assertTrue(porTipo.notificacionesSinHabitacion().isEmpty());
    }
    @Test
    void seguimiento_hastaAntesDeDesde_lanza() {
        assertThrows(IllegalArgumentException.class,
            () -> dashboardService.seguimiento(LocalDate.of(ANIO, 5, 10), LocalDate.of(ANIO, 5, 9), null, null));
    }
}